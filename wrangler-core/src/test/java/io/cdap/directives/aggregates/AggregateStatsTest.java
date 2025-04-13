package io.cdap.wrangler;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.executor.ExecutorContext;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.annotations.PublicEvolving;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AggregateStatsTest {

  @Test
  public void testAggregateStatsDirective() throws Exception {
    AggregateStats directive = new AggregateStats();
    directive.initialize(new String[]{"size", "duration", "total_size", "total_duration", "MB", "seconds"});

    ExecutorContext context = new ExecutorContext() {}; // mock if needed

    Row row1 = new Row("size", new ByteSize("10KB")).add("duration", new TimeDuration("1s"));
    Row row2 = new Row("size", new ByteSize("5MB")).add("duration", new TimeDuration("2s"));
    Row row3 = new Row("size", new ByteSize("0.5GB")).add("duration", new TimeDuration("1.5s"));

    directive.execute(Arrays.asList(row1, row2, row3), context);

    List<Row> result = directive.getAggregate();  // or execute() based on your implementation

    assertEquals(1, result.size());
    Row output = result.get(0);

    assertTrue(output.hasColumn("total_size"));
    assertTrue(output.hasColumn("total_duration"));

    // Check unit conversion
    double totalSizeMB = (10 * 1024 + 5 * 1024 * 1024 + 0.5 * 1024 * 1024 * 1024) / (1024.0 * 1024);
    double totalDurationSec = (1 + 2 + 1.5);

    assertEquals(totalSizeMB, (double) output.getValue("total_size"), 0.001);
    assertEquals(totalDurationSec * 1000, (double) output.getValue("total_duration"), 0.001); // if still in millis
  }
}
