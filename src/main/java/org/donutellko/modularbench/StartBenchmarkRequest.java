package org.donutellko.modularbench;

import lombok.Data;
import java.util.List;

public record StartBenchmarkRequest(
    String execConfig,
    List<String> taskSources,
    String resultFilename
) {}
