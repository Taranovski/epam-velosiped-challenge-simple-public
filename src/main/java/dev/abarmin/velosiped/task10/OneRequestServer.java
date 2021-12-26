package dev.abarmin.velosiped.task10;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

public interface OneRequestServer {
    Map<String, Object> doHandlingAndRecordIntermediateData(InputStream inputStream, PrintWriter bufferedWriter);
}
