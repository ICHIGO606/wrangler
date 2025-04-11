/*
 *  Copyright © 2023 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.aggregates;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * A directive that aggregates byte sizes and time durations from specified columns.
 * 
 * This directive calculates the total byte size and total time duration from the specified
 * columns across all input rows, and outputs a single row with the aggregated values.
 */
@Plugin(type = Directive.TYPE)
@Name(AggregateStats.NAME)
@Categories(categories = {"aggregate", "stats"})
@Description("Aggregates byte sizes and time durations from specified columns.")
public class AggregateStats implements Directive {
  public static final String NAME = "aggregate-stats";
  private static final String SIZE_TOTAL_KEY = "size_total";
  private static final String TIME_TOTAL_KEY = "time_total";
  
  private String sizeColumn;
  private String timeColumn;
  private String totalSizeColumn;
  private String totalTimeColumn;
  private String sizeOutputUnit;
  private String timeOutputUnit;
  
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("size_column", TokenType.COLUMN_NAME);
    builder.define("time_column", TokenType.COLUMN_NAME);
    builder.define("total_size_column", TokenType.COLUMN_NAME);
    builder.define("total_time_column", TokenType.COLUMN_NAME);
    builder.define("size_output_unit", TokenType.TEXT, true);
    builder.define("time_output_unit", TokenType.TEXT, true);
    return builder.build();
  }
  
  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sizeColumn = ((ColumnName) args.value("size_column")).value();
    this.timeColumn = ((ColumnName) args.value("time_column")).value();
    this.totalSizeColumn = ((ColumnName) args.value("total_size_column")).value();
    this.totalTimeColumn = ((ColumnName) args.value("total_time_column")).value();
    
    if (args.contains("size_output_unit")) {
      this.sizeOutputUnit = ((Text) args.value("size_output_unit")).value();
    } else {
      this.sizeOutputUnit = "MB"; // Default to MB
    }
    
    if (args.contains("time_output_unit")) {
      this.timeOutputUnit = ((Text) args.value("time_output_unit")).value();
    } else {
      this.timeOutputUnit = "s"; // Default to seconds
    }
    
    // Validate output units
    if (!isValidSizeUnit(sizeOutputUnit)) {
      throw new DirectiveParseException(
        NAME, String.format("Invalid size output unit '%s'. Valid units are KB, MB, GB, TB.", sizeOutputUnit));
    }
    
    if (!isValidTimeUnit(timeOutputUnit)) {
      throw new DirectiveParseException(
        NAME, String.format("Invalid time output unit '%s'. Valid units are ms, s, m, h, d.", timeOutputUnit));
    }
  }
  
  private boolean isValidSizeUnit(String unit) {
    return unit.equalsIgnoreCase("KB") || 
           unit.equalsIgnoreCase("MB") || 
           unit.equalsIgnoreCase("GB") || 
           unit.equalsIgnoreCase("TB");
  }
  
  private boolean isValidTimeUnit(String unit) {
    return unit.equals("ms") || 
           unit.equals("s") || 
           unit.equals("m") || 
           unit.equals("h") || 
           unit.equals("d");
  }
  
  @Override
  public void destroy() {
    // no-op
  }
  
  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    if (context == null) {
      throw new DirectiveExecutionException(NAME, "Context is null. Cannot execute directive.");
    }
    
    // Initialize or get the running totals from the transient store
    long totalBytes = 0;
    long totalNanos = 0;
    
    if (context.getTransientStore().get(SIZE_TOTAL_KEY) != null) {
      totalBytes = (long) context.getTransientStore().get(SIZE_TOTAL_KEY);
    }
    
    if (context.getTransientStore().get(TIME_TOTAL_KEY) != null) {
      totalNanos = (long) context.getTransientStore().get(TIME_TOTAL_KEY);
    }
    
    // Process each row and accumulate totals
    for (Row row : rows) {
      // Process byte size
      Object sizeObj = row.getValue(sizeColumn);
      if (sizeObj != null) {
        try {
          if (sizeObj instanceof String) {
            // Parse the string as a byte size
            totalBytes += parseByteSize((String) sizeObj);
          } else if (sizeObj instanceof Number) {
            // Assume the number is already in bytes
            totalBytes += ((Number) sizeObj).longValue();
          }
        } catch (Exception e) {
          // Skip invalid values
          context.getMetrics().count("invalid.byte.size", 1);
        }
      }
      
      // Process time duration
      Object timeObj = row.getValue(timeColumn);
      if (timeObj != null) {
        try {
          if (timeObj instanceof String) {
            // Parse the string as a time duration
            totalNanos += parseTimeDuration((String) timeObj);
          } else if (timeObj instanceof Number) {
            // Assume the number is already in milliseconds and convert to nanos
            totalNanos += ((Number) timeObj).longValue() * 1_000_000;
          }
        } catch (Exception e) {
          // Skip invalid values
          context.getMetrics().count("invalid.time.duration", 1);
        }
      }
    }
    
    // Store the updated totals in the transient store
    context.getTransientStore().set(TransientVariableScope.GLOBAL, SIZE_TOTAL_KEY, totalBytes);
    context.getTransientStore().set(TransientVariableScope.GLOBAL, TIME_TOTAL_KEY, totalNanos);
    
    // If this is the last batch of rows, create a result row with the totals
    // For simplicity, we'll always return a result row in this implementation
    List<Row> result = new ArrayList<>();
    Row resultRow = new Row();
    
    // Convert and add the total size in the requested output unit
    double convertedSize = convertBytes(totalBytes, sizeOutputUnit);
    resultRow.add(totalSizeColumn, convertedSize);
    
    // Convert and add the total time in the requested output unit
    double convertedTime = convertTime(totalNanos, timeOutputUnit);
    resultRow.add(totalTimeColumn, convertedTime);
    
    result.add(resultRow);
    return result;
  }
  
  /**
   * Parses a byte size string (e.g., "10KB", "1.5MB") and returns the size in bytes.
   */
  private long parseByteSize(String input) {
    String trimmed = input.trim().toUpperCase();
    double value;
    long multiplier;
    
    if (trimmed.endsWith("KB")) {
      value = Double.parseDouble(trimmed.substring(0, trimmed.length() - 2));
      multiplier = 1024L;
    } else if (trimmed.endsWith("MB")) {
      value = Double.parseDouble(trimmed.substring(0, trimmed.length() - 2));
      multiplier = 1024L * 1024;
    } else if (trimmed.endsWith("GB")) {
      value = Double.parseDouble(trimmed.substring(0, trimmed.length() - 2));
      multiplier = 1024L * 1024 * 1024;
    } else if (trimmed.endsWith("TB")) {
      value = Double.parseDouble(trimmed.substring(0, trimmed.length() - 2));
      multiplier = 1024L * 1024 * 1024 * 1024;
    } else {
      // Assume bytes if no unit is specified
      value = Double.parseDouble(trimmed);
      multiplier = 1L;
    }
    
    return (long) (value * multiplier);
  }
  
  /**
   * Parses a time duration string (e.g., "150ms", "5s") and returns the duration in nanoseconds.
   */
  private long parseTimeDuration(String input) {
    String trimmed = input.trim().toLowerCase();
    long value;
    
    if (trimmed.endsWith("ms")) {
      value = Long.parseLong(trimmed.substring(0, trimmed.length() - 2));
      return Duration.ofMillis(value).toNanos();
    } else if (trimmed.endsWith("s")) {
      value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
      return Duration.ofSeconds(value).toNanos();
    } else if (trimmed.endsWith("m")) {
      value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
      return Duration.ofMinutes(value).toNanos();
    } else if (trimmed.endsWith("h")) {
      value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
      return Duration.ofHours(value).toNanos();
    } else if (trimmed.endsWith("d")) {
      value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
      return Duration.ofDays(value).toNanos();
    } else {
      // Assume milliseconds if no unit is specified
      value = Long.parseLong(trimmed);
      return Duration.ofMillis(value).toNanos();
    }
  }
  
  /**
   * Converts bytes to the specified unit.
   */
  private double convertBytes(long bytes, String unit) {
    if (unit.equalsIgnoreCase("KB")) {
      return bytes / 1024.0;
    } else if (unit.equalsIgnoreCase("MB")) {
      return bytes / (1024.0 * 1024.0);
    } else if (unit.equalsIgnoreCase("GB")) {
      return bytes / (1024.0 * 1024.0 * 1024.0);
    } else if (unit.equalsIgnoreCase("TB")) {
      return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
    } else {
      return bytes; // Default to bytes
    }
  }
  
  /**
   * Converts nanoseconds to the specified time unit.
   */
  private double convertTime(long nanos, String unit) {
    if (unit.equals("ms")) {
      return nanos / 1_000_000.0;
    } else if (unit.equals("s")) {
      return nanos / 1_000_000_000.0;
    } else if (unit.equals("m")) {
      return nanos / (60.0 * 1_000_000_000.0);
    } else if (unit.equals("h")) {
      return nanos / (60.0 * 60.0 * 1_000_000_000.0);
    } else if (unit.equals("d")) {
      return nanos / (24.0 * 60.0 * 60.0 * 1_000_000_000.0);
    } else {
      return nanos; // Default to nanos
    }
  }
}