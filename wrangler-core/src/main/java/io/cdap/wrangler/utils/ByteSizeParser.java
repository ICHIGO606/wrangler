package io.cdap.wrangler.utils;
import java.util.Locale;


public final class ByteSizeParser {

    private ByteSizeParser() {
      // Utility class
    }
  
    public static long parse(String input) {
      String trimmed = input.trim().toUpperCase(Locale.ROOT);
      double value;
      long multiplier;
  
      if (trimmed.endsWith("KB")) {
        value = Double.parseDouble(trimmed.replace("KB", ""));
        multiplier = 1024L;
      } else if (trimmed.endsWith("MB")) {
        value = Double.parseDouble(trimmed.replace("MB", ""));
        multiplier = 1024L * 1024;
      } else if (trimmed.endsWith("GB")) {
        value = Double.parseDouble(trimmed.replace("GB", ""));
        multiplier = 1024L * 1024 * 1024;
      } else if (trimmed.endsWith("TB")) {
        value = Double.parseDouble(trimmed.replace("TB", ""));
        multiplier = 1024L * 1024 * 1024 * 1024;
      } else {
        throw new IllegalArgumentException("Invalid byte size format: " + input);
      }
  
      return (long) (value * multiplier);
    }
  }
