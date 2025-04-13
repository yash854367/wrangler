package io.cdap.wrangler.directive;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AggregateByteTimeTest {

  private Directive directive;

  @BeforeEach
  public void setup() {
    directive = new AggregateByteTime();
  }

  private Arguments createArguments(String sizeCol, String timeCol,
                                    String outSizeCol, String outTimeCol,
                                    String unit, String aggType) {
    return new Arguments() {
      @Override
      public int length() {
        return 6;
      }

      @Override
      public Object value(String key) {
        switch (key) {
          case "arg0": return new ColumnName(sizeCol);
          case "arg1": return new ColumnName(timeCol);
          case "arg2": return new ColumnName(outSizeCol);
          case "arg3": return new ColumnName(outTimeCol);
          case "arg4": return new Text(unit);
          case "arg5": return new Text(aggType);
          default: throw new IllegalArgumentException("Unknown arg: " + key);
        }
      }
    };
  }

  @Test
  public void testTotalAggregation() throws Exception {
    Arguments args = createArguments("size", "time", "total_size", "total_time", "MB", "total");
    directive.initialize(args);
    directive.initialize((ExecutorContext) null); // no-op for now

    List<Row> rows = new ArrayList<>();
    rows.add(new Row("size", "1GB").add("time", "1h"));
    rows.add(new Row("size", "512MB").add("time", "30m"));

    List<Row> result = directive.execute(rows);

    assertEquals(1, result.size());
    Row output = result.get(0);

    // 1GB + 512MB = 1.5 GB = 1536 MB
    assertEquals(1536.0, (double) output.getValue("total_size"), 0.1);

    // 1h + 30m = 90 minutes = 5400000 ms
    // output unit is still 'MB' so time won't be scaled
    assertEquals(5400000.0, (double) output.getValue("total_time"), 0.1);
  }

  @Test
  public void testAverageAggregation() throws Exception {
    Arguments args = createArguments("size", "time", "avg_size", "avg_time", "MB", "avg");
    directive.initialize(args);
    directive.initialize((ExecutorContext) null); // no-op

    List<Row> rows = new ArrayList<>();
    rows.add(new Row("size", "1GB").add("time", "60s"));
    rows.add(new Row("size", "1GB").add("time", "30s"));

    List<Row> result = directive.execute(rows);

    assertEquals(1, result.size());
    Row output = result.get(0);

    // Avg size = 1GB = 1024MB
    assertEquals(1024.0, (double) output.getValue("avg_size"), 0.1);

    // Avg time = (60 + 30)/2 = 45s = 45000 ms
    assertEquals(45000.0, (double) output.getValue("avg_time"), 0.1);
  }

  @Test
  public void testWithNumericInput() throws Exception {
    Arguments args = createArguments("size", "time", "sum_size", "sum_time", "KB", "total");
    directive.initialize(args);
    directive.initialize((ExecutorContext) null); // no-op

    List<Row> rows = Arrays.asList(
        new Row("size", 2048L).add("time", 2000L), // 2048 bytes = 2KB
        new Row("size", 1024L).add("time", 3000L)  // 1024 bytes = 1KB
    );

    List<Row> result = directive.execute(rows);

    assertEquals(1, result.size());
    Row output = result.get(0);

    assertEquals(3.0, (double) output.getValue("sum_size"), 0.1); // KB
    assertEquals(5000.0, (double) output.getValue("sum_time"), 0.1); // ms
  }
}
