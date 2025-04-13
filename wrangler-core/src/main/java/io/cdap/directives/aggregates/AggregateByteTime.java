package io.cdap.wrangler.directive;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.annotations.UserDirective;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;

import java.util.List;
import java.util.ArrayList;

@UserDirective(name = "aggregate-bytes-time", usage = "aggregate-bytes-time <size-col> <time-col> <output-size-col> <output-time-col> [unit=MB|GB] [agg=total|avg]", description = "Aggregates byte size and time duration columns")
public class AggregateByteTime implements Directive {

    private String sizeColumn;
    private String timeColumn;
    private String outputSizeColumn;
    private String outputTimeColumn;
    private String unit = "MB"; // default unit
    private String aggType = "total"; // default aggregation
    private transient ExecutorContext context;

    private long totalSize = 0L;
    private long totalTime = 0L;
    private long rowCount = 0L;

    @Override
    public void initialize(Arguments arguments) {
        this.sizeColumn = ((ColumnName) arguments.value("arg0")).value();
        this.timeColumn = ((ColumnName) arguments.value("arg1")).value();
        this.outputSizeColumn = ((ColumnName) arguments.value("arg2")).value();
        this.outputTimeColumn = ((ColumnName) arguments.value("arg3")).value();

        if (arguments.length() > 4 && arguments.value("arg4").type() == TokenType.STRING) {
            this.unit = ((Text) arguments.value("arg4")).value().toUpperCase();
        }

        if (arguments.length() > 5 && arguments.value("arg5").type() == TokenType.STRING) {
            this.aggType = ((Text) arguments.value("arg5")).value().toLowerCase();
        }
    }

    @Override
    public void initialize(ExecutorContext context) {
        this.context = context;
    }

    @Override
    public List<Row> execute(List<Row> rows) {
        for (Row row : rows) {
            Object sizeObj = row.getValue(sizeColumn);
            Object timeObj = row.getValue(timeColumn);

            long sizeBytes = convertToBytes(sizeObj);
            long timeMillis = convertToMillis(timeObj);

            totalSize += sizeBytes;
            totalTime += timeMillis;
            rowCount++;
        }

        double finalSize = formatSize(totalSize, unit);
        double finalTime = formatTime(totalTime, unit);

        if ("avg".equalsIgnoreCase(aggType) && rowCount > 0) {
            finalSize = formatSize(totalSize / rowCount, unit);
            finalTime = formatTime(totalTime / rowCount, unit);
        }

        List<Row> output = new ArrayList<>();
        Row result = new Row();
        result.add(outputSizeColumn, finalSize);
        result.add(outputTimeColumn, finalTime);
        output.add(result);

        return output;
    }

    private long convertToBytes(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue(); // assume already in bytes
        } else {
            return new io.cdap.wrangler.api.parser.ByteSize(value.toString()).getBytes();
        }
    }

    private long convertToMillis(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue(); // assume already in ms
        } else {
            return new io.cdap.wrangler.api.parser.TimeDuration(value.toString()).getMilliseconds();
        }
    }

    private double formatSize(long bytes, String unit) {
        switch (unit) {
            case "GB": return bytes / (1024.0 * 1024 * 1024);
            case "MB": return bytes / (1024.0 * 1024);
            case "KB": return bytes / 1024.0;
            default: return bytes;
        }
    }

    private double formatTime(long millis, String unit) {
        switch (unit) {
            case "H": return millis / (1000.0 * 60 * 60);
            case "M": return millis / (1000.0 * 60);
            case "S": return millis / 1000.0;
            case "MS":
            default: return millis;
        }
    }

    @Override
    public void destroy() {
        // cleanup if needed
    }
}
