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

import java.time.Duration;

/**
 * Tests for {@link TimeDuration} token class.
 */
public class TimeDurationTest {

  @Test
  public void testTimeDurationConstruction() {
    // Test basic construction
    TimeDuration timeDuration = new TimeDuration("150ms");
    Assert.assertEquals("150ms", timeDuration.value());
    Assert.assertEquals(TokenType.TIME_DURATION, timeDuration.type());
  }
  
  @Test
  public void testTimeDurationValues() {
    // Test milliseconds
    TimeDuration ms = new TimeDuration("150ms");
    Assert.assertEquals(150, ms.getMilliseconds());
    Assert.assertEquals(0, ms.getSeconds()); // Less than 1 second
    
    // Test seconds
    TimeDuration s = new TimeDuration("5s");
    Assert.assertEquals(5000, s.getMilliseconds());
    Assert.assertEquals(5, s.getSeconds());
    Assert.assertEquals(0, s.getMinutes()); // Less than 1 minute
    
    // Test minutes
    TimeDuration m = new TimeDuration("2m");
    Assert.assertEquals(120000, m.getMilliseconds());
    Assert.assertEquals(120, m.getSeconds());
    Assert.assertEquals(2, m.getMinutes());
    Assert.assertEquals(0, m.getHours()); // Less than 1 hour
    
    // Test hours
    TimeDuration h = new TimeDuration("3h");
    Assert.assertEquals(3 * 60 * 60 * 1000, h.getMilliseconds());
    Assert.assertEquals(3 * 60 * 60, h.getSeconds());
    Assert.assertEquals(3 * 60, h.getMinutes());
    Assert.assertEquals(3, h.getHours());
    Assert.assertEquals(0, h.getDays()); // Less than 1 day
    
    // Test days
    TimeDuration d = new TimeDuration("2d");
    Assert.assertEquals(2 * 24 * 60 * 60 * 1000, d.getMilliseconds());
    Assert.assertEquals(2 * 24 * 60 * 60, d.getSeconds());
    Assert.assertEquals(2 * 24 * 60, d.getMinutes());
    Assert.assertEquals(2 * 24, d.getHours());
    Assert.assertEquals(2, d.getDays());
  }
  
  @Test
  public void testCaseInsensitivity() {
    // Test case insensitivity
    TimeDuration ms1 = new TimeDuration("100ms");
    TimeDuration ms2 = new TimeDuration("100MS");
    Assert.assertEquals(ms1.getMilliseconds(), ms2.getMilliseconds());
    
    TimeDuration s1 = new TimeDuration("5s");
    TimeDuration s2 = new TimeDuration("5S");
    Assert.assertEquals(s1.getSeconds(), s2.getSeconds());
  }
  
  @Test
  public void testWhitespaceHandling() {
    // Test whitespace handling
    TimeDuration ms1 = new TimeDuration("100ms");
    TimeDuration ms2 = new TimeDuration(" 100ms ");
    Assert.assertEquals(ms1.getMilliseconds(), ms2.getMilliseconds());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    // Test with invalid format (no unit)
    new TimeDuration("100");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    // Test with invalid unit
    new TimeDuration("10x");
  }
  
  @Test
  public void testJsonSerialization() {
    // Test JSON serialization
    TimeDuration timeDuration = new TimeDuration("150ms");
    Assert.assertTrue(timeDuration.toJson().isJsonObject());
    Assert.assertEquals(TokenType.TIME_DURATION.name(), 
                        timeDuration.toJson().getAsJsonObject().get("type").getAsString());
    Assert.assertEquals("150ms", 
                        timeDuration.toJson().getAsJsonObject().get("value").getAsString());
    Assert.assertEquals(150, 
                        timeDuration.toJson().getAsJsonObject().get("milliseconds").getAsLong());
  }
  
  @Test
  public void testDurationObject() {
    // Test getting the Duration object
    TimeDuration ms = new TimeDuration("150ms");
    Duration duration = ms.getDuration();
    Assert.assertEquals(150, duration.toMillis());
    
    TimeDuration s = new TimeDuration("5s");
    duration = s.getDuration();
    Assert.assertEquals(5, duration.getSeconds());
  }
}