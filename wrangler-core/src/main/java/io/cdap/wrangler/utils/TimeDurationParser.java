package io.cdap.wrangler.utils;

import java.time.Duration;
import java.util.Locale;

/**
 * Parses time duration strings like "100ms", "5s", "3m", "2h", "1d".
 */
public final class TimeDurationParser {

  private TimeDurationParser() {
    // Utility class
  }

  public static Duration parse(String input) {
    String trimmed = input.trim().toLowerCase(Locale.ROOT);

    if (trimmed.endsWith("ms")) {
      long val = Long.parseLong(trimmed.replace("ms", ""));
      return Duration.ofMillis(val);
    } else if (trimmed.endsWith("s")) {
      long val = Long.parseLong(trimmed.replace("s", ""));
      return Duration.ofSeconds(val);
    } else if (trimmed.endsWith("m")) {
      long val = Long.parseLong(trimmed.replace("m", ""));
      return Duration.ofMinutes(val);
    } else if (trimmed.endsWith("h")) {
      long val = Long.parseLong(trimmed.replace("h", ""));
      return Duration.ofHours(val);
    } else if (trimmed.endsWith("d")) {
      long val = Long.parseLong(trimmed.replace("d", ""));
      return Duration.ofDays(val);
    } else {
      throw new IllegalArgumentException("Invalid time duration format: " + input);
    }
  }
}
