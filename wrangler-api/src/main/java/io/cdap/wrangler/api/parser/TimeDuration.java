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

import java.time.Duration;

/**
 * This class represents a time duration token with units (ms, s, m, h, d).
 * It parses strings like "150ms", "5s", etc. and provides methods to
 * retrieve the duration in various time units.
 */
@PublicEvolving
public class TimeDuration implements Token {
  private final String value;
  private final Duration duration;

  /**
   * Constructs a TimeDuration token from a string representation.
   *
   * @param value String representation of time duration (e.g., "150ms", "5s")
   */
  public TimeDuration(String value) {
    this.value = value;
    this.duration = parseDuration(value);
  }

  /**
   * Parses the time duration string and converts it to a Duration object.
   *
   * @param input String representation of time duration
   * @return The Duration object
   */
  private Duration parseDuration(String input) {
    String trimmed = input.trim().toLowerCase();

    if (trimmed.endsWith("ms")) {
      long val = Long.parseLong(trimmed.substring(0, trimmed.length() - 2));
      return Duration.ofMillis(val);
    } else if (trimmed.endsWith("s")) {
      long val = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
      return Duration.ofSeconds(val);
    } else if (trimmed.endsWith("m")) {
      long val = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
      return Duration.ofMinutes(val);
    } else if (trimmed.endsWith("h")) {
      long val = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
      return Duration.ofHours(val);
    } else if (trimmed.endsWith("d")) {
      long val = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
      return Duration.ofDays(val);
    } else {
      throw new IllegalArgumentException("Invalid time duration format: " + input);
    }
  }

  /**
   * Returns the original string value of the time duration.
   *
   * @return String representation of the time duration
   */
  @Override
  public String value() {
    return value;
  }

  /**
   * Returns the token type for this token.
   *
   * @return TokenType.TIME_DURATION
   */
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  /**
   * Returns the Duration object.
   *
   * @return The Duration object
   */
  public Duration getDuration() {
    return duration;
  }

  /**
   * Returns the duration in milliseconds.
   *
   * @return The duration in milliseconds
   */
  public long getMilliseconds() {
    return duration.toMillis();
  }

  /**
   * Returns the duration in seconds.
   *
   * @return The duration in seconds
   */
  public long getSeconds() {
    return duration.getSeconds();
  }

  /**
   * Returns the duration in minutes.
   *
   * @return The duration in minutes
   */
  public long getMinutes() {
    return duration.toMinutes();
  }

  /**
   * Returns the duration in hours.
   *
   * @return The duration in hours
   */
  public long getHours() {
    return duration.toHours();
  }

  /**
   * Returns the duration in days.
   *
   * @return The duration in days
   */
  public long getDays() {
    return duration.toDays();
  }

  /**
   * Converts this token to a JSON representation.
   *
   * @return JSON representation of this token
   */
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.TIME_DURATION.name());
    object.addProperty("value", value);
    object.addProperty("milliseconds", getMilliseconds());
    return object;
  }
}