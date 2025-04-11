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

package io.cdap.wrangler.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * Tests for {@link TimeDurationParser} utility class.
 */
public class TimeDurationParserTest {

  @Test
  public void testParseMilliseconds() {
    // Test parsing milliseconds
    Duration duration = TimeDurationParser.parse("100ms");
    Assert.assertEquals(100, duration.toMillis());
    
    // Test with whitespace
    duration = TimeDurationParser.parse(" 50ms ");
    Assert.assertEquals(50, duration.toMillis());
    
    // Test with uppercase
    duration = TimeDurationParser.parse("75MS");
    Assert.assertEquals(75, duration.toMillis());
  }
  
  @Test
  public void testParseSeconds() {
    // Test parsing seconds
    Duration duration = TimeDurationParser.parse("5s");
    Assert.assertEquals(5, duration.getSeconds());
    Assert.assertEquals(5000, duration.toMillis());
    
    // Test with uppercase
    duration = TimeDurationParser.parse("10S");
    Assert.assertEquals(10, duration.getSeconds());
  }
  
  @Test
  public void testParseMinutes() {
    // Test parsing minutes
    Duration duration = TimeDurationParser.parse("2m");
    Assert.assertEquals(2, duration.toMinutes());
    Assert.assertEquals(120, duration.getSeconds());
    
    // Test with uppercase
    duration = TimeDurationParser.parse("3M");
    Assert.assertEquals(3, duration.toMinutes());
  }
  
  @Test
  public void testParseHours() {
    // Test parsing hours
    Duration duration = TimeDurationParser.parse("1h");
    Assert.assertEquals(1, duration.toHours());
    Assert.assertEquals(60, duration.toMinutes());
    
    // Test with uppercase
    duration = TimeDurationParser.parse("2H");
    Assert.assertEquals(2, duration.toHours());
  }
  
  @Test
  public void testParseDays() {
    // Test parsing days
    Duration duration = TimeDurationParser.parse("1d");
    Assert.assertEquals(1, duration.toDays());
    Assert.assertEquals(24, duration.toHours());
    
    // Test with uppercase
    duration = TimeDurationParser.parse("2D");
    Assert.assertEquals(2, duration.toDays());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    // Test with invalid format (no unit)
    TimeDurationParser.parse("100");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    // Test with invalid unit
    TimeDurationParser.parse("10x");
  }
  
  @Test(expected = NumberFormatException.class)
  public void testInvalidNumber() {
    // Test with invalid number
    TimeDurationParser.parse("abc ms");
  }
}