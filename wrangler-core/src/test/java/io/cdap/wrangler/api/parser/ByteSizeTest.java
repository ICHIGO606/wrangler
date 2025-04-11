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

package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ByteSize} token class.
 */
public class ByteSizeTest {

  @Test
  public void testByteSizeConstruction() {
    // Test basic construction
    ByteSize byteSize = new ByteSize("10KB");
    Assert.assertEquals("10KB", byteSize.value());
    Assert.assertEquals(TokenType.BYTE_SIZE, byteSize.type());
  }
  
  @Test
  public void testByteSizeValues() {
    // Test kilobytes
    ByteSize kb = new ByteSize("10KB");
    Assert.assertEquals(10 * 1024L, kb.getBytes());
    Assert.assertEquals(10.0, kb.getKilobytes(), 0.001);
    Assert.assertEquals(10.0 / 1024, kb.getMegabytes(), 0.001);
    
    // Test megabytes
    ByteSize mb = new ByteSize("1.5MB");
    Assert.assertEquals((long)(1.5 * 1024 * 1024), mb.getBytes());
    Assert.assertEquals(1.5 * 1024, mb.getKilobytes(), 0.001);
    Assert.assertEquals(1.5, mb.getMegabytes(), 0.001);
    
    // Test gigabytes
    ByteSize gb = new ByteSize("2GB");
    Assert.assertEquals(2 * 1024L * 1024L * 1024L, gb.getBytes());
    Assert.assertEquals(2 * 1024 * 1024, gb.getKilobytes(), 0.001);
    Assert.assertEquals(2 * 1024, gb.getMegabytes(), 0.001);
    Assert.assertEquals(2.0, gb.getGigabytes(), 0.001);
    
    // Test terabytes
    ByteSize tb = new ByteSize("0.5TB");
    Assert.assertEquals((long)(0.5 * 1024 * 1024 * 1024 * 1024), tb.getBytes());
    Assert.assertEquals(0.5 * 1024 * 1024 * 1024, tb.getKilobytes(), 0.001);
    Assert.assertEquals(0.5 * 1024 * 1024, tb.getMegabytes(), 0.001);
    Assert.assertEquals(0.5 * 1024, tb.getGigabytes(), 0.001);
    Assert.assertEquals(0.5, tb.getTerabytes(), 0.001);
  }
  
  @Test
  public void testCaseInsensitivity() {
    // Test case insensitivity
    ByteSize kb1 = new ByteSize("5KB");
    ByteSize kb2 = new ByteSize("5kb");
    Assert.assertEquals(kb1.getBytes(), kb2.getBytes());
    
    ByteSize mb1 = new ByteSize("1MB");
    ByteSize mb2 = new ByteSize("1mb");
    Assert.assertEquals(mb1.getBytes(), mb2.getBytes());
  }
  
  @Test
  public void testWhitespaceHandling() {
    // Test whitespace handling
    ByteSize kb1 = new ByteSize("10KB");
    ByteSize kb2 = new ByteSize(" 10KB ");
    Assert.assertEquals(kb1.getBytes(), kb2.getBytes());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    // Test with invalid format (no unit)
    new ByteSize("100");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    // Test with invalid unit
    new ByteSize("10XB");
  }
  
  @Test
  public void testJsonSerialization() {
    // Test JSON serialization
    ByteSize byteSize = new ByteSize("10KB");
    Assert.assertTrue(byteSize.toJson().isJsonObject());
    Assert.assertEquals(TokenType.BYTE_SIZE.name(), 
                        byteSize.toJson().getAsJsonObject().get("type").getAsString());
    Assert.assertEquals("10KB", 
                        byteSize.toJson().getAsJsonObject().get("value").getAsString());
    Assert.assertEquals(10 * 1024L, 
                        byteSize.toJson().getAsJsonObject().get("bytes").getAsLong());
  }
}