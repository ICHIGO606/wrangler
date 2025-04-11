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

/**
 * Tests for {@link ByteSizeParser} utility class.
 */
public class ByteSizeParserTest {

  @Test
  public void testParseKilobytes() {
    // Test parsing kilobytes
    long bytes = ByteSizeParser.parse("10KB");
    Assert.assertEquals(10 * 1024L, bytes);
    
    // Test with decimal value
    bytes = ByteSizeParser.parse("1.5KB");
    Assert.assertEquals((long)(1.5 * 1024), bytes);
    
    // Test with whitespace
    bytes = ByteSizeParser.parse(" 5KB ");
    Assert.assertEquals(5 * 1024L, bytes);
    
    // Test with lowercase
    bytes = ByteSizeParser.parse("2kb");
    Assert.assertEquals(2 * 1024L, bytes);
  }
  
  @Test
  public void testParseMegabytes() {
    // Test parsing megabytes
    long bytes = ByteSizeParser.parse("10MB");
    Assert.assertEquals(10 * 1024L * 1024L, bytes);
    
    // Test with decimal value
    bytes = ByteSizeParser.parse("1.5MB");
    Assert.assertEquals((long)(1.5 * 1024 * 1024), bytes);
  }
  
  @Test
  public void testParseGigabytes() {
    // Test parsing gigabytes
    long bytes = ByteSizeParser.parse("2GB");
    Assert.assertEquals(2 * 1024L * 1024L * 1024L, bytes);
    
    // Test with decimal value
    bytes = ByteSizeParser.parse("0.5GB");
    Assert.assertEquals((long)(0.5 * 1024 * 1024 * 1024), bytes);
  }
  
  @Test
  public void testParseTerabytes() {
    // Test parsing terabytes
    long bytes = ByteSizeParser.parse("1TB");
    Assert.assertEquals(1024L * 1024L * 1024L * 1024L, bytes);
    
    // Test with decimal value
    bytes = ByteSizeParser.parse("0.25TB");
    Assert.assertEquals((long)(0.25 * 1024 * 1024 * 1024 * 1024), bytes);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    // Test with invalid format (no unit)
    ByteSizeParser.parse("100");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    // Test with invalid unit
    ByteSizeParser.parse("10XB");
  }
  
  @Test(expected = NumberFormatException.class)
  public void testInvalidNumber() {
    // Test with invalid number
    ByteSizeParser.parse("abc KB");
  }
}