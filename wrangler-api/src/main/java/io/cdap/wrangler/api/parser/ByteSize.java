package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ByteSize implements Token {
  private final long bytes;
  private final String original;

  public ByteSize(String value) {
    this.original = value;
    this.bytes = parse(value);
  }

  private long parse(String val) {
    val = val.trim().toUpperCase();
    double number;
    long multiplier;

    if (val.endsWith("KB")) {
      number = Double.parseDouble(val.replace("KB", ""));
      multiplier = 1024L;
    } else if (val.endsWith("MB")) {
      number = Double.parseDouble(val.replace("MB", ""));
      multiplier = 1024L * 1024;
    } else if (val.endsWith("GB")) {
      number = Double.parseDouble(val.replace("GB", ""));
      multiplier = 1024L * 1024 * 1024;
    } else if (val.endsWith("TB")) {
      number = Double.parseDouble(val.replace("TB", ""));
      multiplier = 1024L * 1024 * 1024 * 1024;
    } else {
      throw new IllegalArgumentException("Invalid byte size format: " + val);
    }

    return (long) (number * multiplier);
  }

  public long getBytes() {
    return bytes;
  }

  @Override
  public Object value() {
    return bytes;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(bytes);
  }
}
