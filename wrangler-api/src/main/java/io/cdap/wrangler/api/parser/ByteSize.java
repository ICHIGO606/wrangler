/*
 * Copyright © 2023 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * This class represents a byte size token with units (KB, MB, GB, TB).
 * It parses strings like "10KB", "1.5MB", etc. and provides methods to
 * retrieve the value in bytes.
 */
@PublicEvolving
public class ByteSize implements Token {
  private final String value;
  private final long bytes;

  /**
   * Constructs a ByteSize token from a string representation.
   *
   * @param value String representation of byte size (e.g., "10KB", "1.5MB")
   */
  public ByteSize(String value) {
    this.value = value;
    this.bytes = parseBytes(value);
  }

  /**
   * Parses the byte size string and converts it to bytes.
   *
   * @param input String representation of byte size
   * @return The size in bytes
   */
  private long parseBytes(String input) {
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
      throw new IllegalArgumentException("Invalid byte size format: " + input);
    }

    return (long) (value * multiplier);
  }

  /**
   * Returns the original string value of the byte size.
   *
   * @return String representation of the byte size
   */
  @Override
  public String value() {
    return value;
  }

  /**
   * Returns the token type for this token.
   *
   * @return TokenType.BYTE_SIZE
   */
  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  /**
   * Returns the size in bytes.
   *
   * @return The size in bytes
   */
  public long getBytes() {
    return bytes;
  }

  /**
   * Returns the size in kilobytes.
   *
   * @return The size in kilobytes
   */
  public double getKilobytes() {
    return bytes / 1024.0;
  }

  /**
   * Returns the size in megabytes.
   *
   * @return The size in megabytes
   */
  public double getMegabytes() {
    return bytes / (1024.0 * 1024);
  }

  /**
   * Returns the size in gigabytes.
   *
   * @return The size in gigabytes
   */
  public double getGigabytes() {
    return bytes / (1024.0 * 1024 * 1024);
  }

  /**
   * Returns the size in terabytes.
   *
   * @return The size in terabytes
   */
  public double getTerabytes() {
    return bytes / (1024.0 * 1024 * 1024 * 1024);
  }

  /**
   * Converts this token to a JSON representation.
   *
   * @return JSON representation of this token
   */
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.BYTE_SIZE.name());
    object.addProperty("value", value);
    object.addProperty("bytes", bytes);
    return object;
  }
}