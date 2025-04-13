package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

public class TimeDurationTest {

  @Test
  public void testMilliseconds() {
    TimeDuration td = new TimeDuration("5ms");
    Assert.assertEquals(5L, td.getMilliseconds());
  }

  @Test
  public void testSeconds() {
    TimeDuration td = new TimeDuration("2.1s");
    Assert.assertEquals(2100L, td.getMilliseconds());
  }

  @Test
  public void testMinutes() {
    TimeDuration td = new TimeDuration("1.5m");
    Assert.assertEquals(90000L, td.getMilliseconds());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidInput() {
    new TimeDuration("123xy");
  }
}
