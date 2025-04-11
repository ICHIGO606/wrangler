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

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link AggregateStats} directive.
 */
public class AggregateStatsTest {

  @Test
  public void testAggregateStats() throws Exception {
    // Create test data
    List<Row> rows = new ArrayList<>();
    
    Row row1 = new Row();
    row1.add("data_transfer_size", "10KB");
    row1.add("response_time", "150ms");
    rows.add(row1);
    
    Row row2 = new Row();
    row2.add("data_transfer_size", "1.5MB");
    row2.add("response_time", "2s");
    rows.add(row2);
    
    Row row3 = new Row();
    row3.add("data_transfer_size", "500KB");
    row3.add("response_time", "75ms");
    rows.add(row3);
    
    // Define the recipe
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time :total_size_mb :total_time_sec"
    };
    
    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows);
    
    // Verify results
    Assert.assertEquals(1, results.size());
    Row resultRow = results.get(0);
    
    // Expected values:
    // 10KB + 1.5MB + 500KB = 10 * 1024 + 1.5 * 1024 * 1024 + 500 * 1024 = 2,088,960 bytes = ~1.99 MB
    // 150ms + 2s + 75ms = 150 + 2000 + 75 = 2225 ms = 2.225 seconds
    double totalSizeMB = (Double) resultRow.getValue("total_size_mb");
    double totalTimeSec = (Double) resultRow.getValue("total_time_sec");
    
    // Allow for small floating point differences
    Assert.assertEquals(1.99, totalSizeMB, 0.01);
    Assert.assertEquals(2.225, totalTimeSec, 0.001);
  }
  
  @Test
  public void testAggregateStatsWithCustomUnits() throws Exception {
    // Create test data
    List<Row> rows = new ArrayList<>();
    
    Row row1 = new Row();
    row1.add("data_size", "2GB");
    row1.add("processing_time", "30s");
    rows.add(row1);
    
    Row row2 = new Row();
    row2.add("data_size", "1GB");
    row2.add("processing_time", "1m");
    rows.add(row2);
    
    // Define the recipe with custom output units
    String[] recipe = new String[] {
      "aggregate-stats :data_size :processing_time :total_size_gb :total_time_min 'GB' 'm'"
    };
    
    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows);
    
    // Verify results
    Assert.assertEquals(1, results.size());
    Row resultRow = results.get(0);
    
    // Expected values:
    // 2GB + 1GB = 3GB
    // 30s + 1m = 30 + 60 = 90 seconds = 1.5 minutes
    double totalSizeGB = (Double) resultRow.getValue("total_size_gb");
    double totalTimeMin = (Double) resultRow.getValue("total_time_min");
    
    // Allow for small floating point differences
    Assert.assertEquals(3.0, totalSizeGB, 0.01);
    Assert.assertEquals(1.5, totalTimeMin, 0.01);
  }
  
  @Test
  public void testAggregateStatsWithMixedUnits() throws Exception {
    // Create test data with mixed units
    List<Row> rows = new ArrayList<>();
    
    Row row1 = new Row();
    row1.add("size", "10KB");
    row1.add("time", "100ms");
    rows.add(row1);
    
    Row row2 = new Row();
    row2.add("size", "1MB");
    row2.add("time", "1s");
    rows.add(row2);
    
    Row row3 = new Row();
    row3.add("size", "5KB");
    row3.add("time", "50ms");
    rows.add(row3);
    
    // Define the recipe
    String[] recipe = new String[] {
      "aggregate-stats :size :time :total_size_kb :total_time_ms 'KB' 'ms'"
    };
    
    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows);
    
    // Verify results
    Assert.assertEquals(1, results.size());
    Row resultRow = results.get(0);
    
    // Expected values:
    // 10KB + 1MB + 5KB = 10 + 1024 + 5 = 1039 KB
    // 100ms + 1s + 50ms = 100 + 1000 + 50 = 1150 ms
    double totalSizeKB = (Double) resultRow.getValue("total_size_kb");
    double totalTimeMs = (Double) resultRow.getValue("total_time_ms");
    
    // Allow for small floating point differences
    Assert.assertEquals(1039.0, totalSizeKB, 0.1);
    Assert.assertEquals(1150.0, totalTimeMs, 0.1);
  }
}