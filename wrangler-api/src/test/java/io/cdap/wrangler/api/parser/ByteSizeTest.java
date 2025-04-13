package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTest {

  @Test
  public void testKB() {
    ByteSize bs = new ByteSize("10kb");
    Assert.assertEquals(10240L, bs.getBytes());
  }

  @Test
  public void testMB() {
    ByteSize bs = new ByteSize("1.5MB");
    Assert.assertEquals(1572864L, bs.getBytes());
  }

  @Test
  public void testGB() {
    ByteSize bs = new ByteSize("2GB");
    Assert.assertEquals(2147483648L, bs.getBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidInput() {
    new ByteSize("100XYZ");
  }
}
