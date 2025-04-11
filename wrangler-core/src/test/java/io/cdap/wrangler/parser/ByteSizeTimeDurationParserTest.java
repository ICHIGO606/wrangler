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

package io.cdap.wrangler.parser;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.CompileStatus;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.RecipeParser;
import io.cdap.wrangler.api.RecipeSymbol;
import io.cdap.wrangler.api.TokenGroup;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.Token;
import io.cdap.wrangler.api.RecipeException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/**
 * Tests for parsing ByteSize and TimeDuration tokens in the grammar.
 */
public class ByteSizeTimeDurationParserTest {

  @Test
  public void testByteSizeTokenParsing() throws Exception {
    // Test recipe with ByteSize tokens
    String[] recipe = new String[] {
      "aggregate-stats :data_size :processing_time :total_size_gb :total_time_min 10KB 5s"
    };

    // Parse the recipe and get the compile status
    CompileStatus status = TestingRig.compile(recipe);
    Assert.assertTrue(status.isSuccess());
    
    // Get the tokens from the first directive
    RecipeSymbol symbols = status.getSymbols();
    Iterator<TokenGroup> tokenGroups = symbols.iterator();
    Assert.assertTrue(tokenGroups.hasNext());
    TokenGroup tokenGroup = tokenGroups.next();
    
    // Verify ByteSize token was parsed correctly
    boolean foundByteSize = false;
    Iterator<Token> tokens = tokenGroup.iterator();
    while (tokens.hasNext()) {
      Token token = tokens.next();
      if (token instanceof ByteSize) {
        foundByteSize = true;
        ByteSize byteSize = (ByteSize) token;
        Assert.assertEquals("10KB", byteSize.value());
        Assert.assertEquals(10 * 1024L, byteSize.getBytes());
        break;
      }
    }
    Assert.assertTrue("ByteSize token not found", foundByteSize);
  }

  @Test
  public void testTimeDurationTokenParsing() throws Exception {
    // Test recipe with TimeDuration tokens
    String[] recipe = new String[] {
      "aggregate-stats :data_size :processing_time :total_size_gb :total_time_min 10KB 5s"
    };

    // Parse the recipe and get the compile status
    CompileStatus status = TestingRig.compile(recipe);
    Assert.assertTrue(status.isSuccess());
    
    // Get the tokens from the first directive
    RecipeSymbol symbols = status.getSymbols();
    Iterator<TokenGroup> tokenGroups = symbols.iterator();
    Assert.assertTrue(tokenGroups.hasNext());
    TokenGroup tokenGroup = tokenGroups.next();
    
    // Verify TimeDuration token was parsed correctly
    boolean foundTimeDuration = false;
    Iterator<Token> tokens = tokenGroup.iterator();
    while (tokens.hasNext()) {
      Token token = tokens.next();
      if (token instanceof TimeDuration) {
        foundTimeDuration = true;
        TimeDuration timeDuration = (TimeDuration) token;
        Assert.assertEquals("5s", timeDuration.value());
        Assert.assertEquals(5, timeDuration.getSeconds());
        break;
      }
    }
    Assert.assertTrue("TimeDuration token not found", foundTimeDuration);
  }

  @Test
  public void testMultipleTokenTypes() throws Exception {
    // Test recipe with multiple token types
    String[] recipe = new String[] {
      "aggregate-stats :data_size :processing_time :total_size_gb :total_time_min 1.5MB 150ms"
    };

    // Parse the recipe and get the compile status
    CompileStatus status = TestingRig.compile(recipe);
    Assert.assertTrue(status.isSuccess());
    
    // Get the tokens from the first directive
    RecipeSymbol symbols = status.getSymbols();
    Iterator<TokenGroup> tokenGroups = symbols.iterator();
    Assert.assertTrue(tokenGroups.hasNext());
    TokenGroup tokenGroup = tokenGroups.next();
    
    // Count token types
    int byteSizeCount = 0;
    int timeDurationCount = 0;
    
    Iterator<Token> tokens = tokenGroup.iterator();
    while (tokens.hasNext()) {
      Token token = tokens.next();
      if (token instanceof ByteSize) {
        byteSizeCount++;
        ByteSize byteSize = (ByteSize) token;
        Assert.assertEquals("1.5MB", byteSize.value());
      } else if (token instanceof TimeDuration) {
        timeDurationCount++;
        TimeDuration timeDuration = (TimeDuration) token;
        Assert.assertEquals("150ms", timeDuration.value());
      }
    }
    
    Assert.assertEquals("Expected 1 ByteSize token", 1, byteSizeCount);
    Assert.assertEquals("Expected 1 TimeDuration token", 1, timeDurationCount);
  }

  @Test(expected = RecipeException.class)
  public void testInvalidSyntax() throws Exception {
    // Test recipe with invalid syntax
    String[] recipe = new String[] {
      "aggregate-stats :data_size :processing_time :total_size_gb :total_time_min 10XB 5s"
    };

    // This should throw RecipeException due to invalid ByteSize format
    TestingRig.compile(recipe);
  }
}