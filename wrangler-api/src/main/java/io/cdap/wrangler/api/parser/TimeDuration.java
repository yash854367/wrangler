package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class TimeDuration implements Token {
  private final long millis;
  private final String original;

  public TimeDuration(String value) {
    this.original = value;
    this.millis = parse(value);
  }

  private long parse(String val) {
    val = val.trim().toLowerCase();
    double number;
    long multiplier;

    if (val.endsWith("ms")) {
      number = Double.parseDouble(val.replace("ms", ""));
      multiplier = 1L;
    } else if (val.endsWith("s")) {
      number = Double.parseDouble(val.replace("s", ""));
      multiplier = 1000L;
    } else if (val.endsWith("m")) {
      number = Double.parseDouble(val.replace("m", ""));
      multiplier = 60L * 1000;
    } else if (val.endsWith("h")) {
      number = Double.parseDouble(val.replace("h", ""));
      multiplier = 60L * 60 * 1000;
    } else {
      throw new IllegalArgumentException("Invalid time duration format: " + val);
    }

    return (long) (number * multiplier);
  }

  public long getMilliseconds() {
    return millis;
  }

  @Override
  public Object value() {
    return millis;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(millis);
  }
}
