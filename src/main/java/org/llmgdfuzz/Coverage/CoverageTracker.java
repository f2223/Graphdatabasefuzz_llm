package org.llmgdfuzz.Coverage;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.data.ExecutionDataReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CoverageTracker {

    private final Set<Long> seenClassIds = new HashSet<>();
    private int totalProbeCount = 0;

    public CoverageTracker() {
        startHourlyCoverageLogger();
    }

    public int getNewCoverageCount() throws Exception {
        IAgent agent = RT.getAgent();
        byte[] execData = agent.getExecutionData(false); // false = 不重置探针状态

        // 读取并分析数据
        ExecutionDataStore eds = new ExecutionDataStore();
        SessionInfoStore sis = new SessionInfoStore();

        try (InputStream in = new ByteArrayInputStream(execData)) {
            ExecutionDataReader reader = new ExecutionDataReader(in);
            reader.setExecutionDataVisitor(eds);
            reader.setSessionInfoVisitor(sis);
            reader.read();
        }

        int newCount = 0;
        for (var ed : eds.getContents()) {
            long id = ed.getId();
            if (!seenClassIds.contains(id)) {
                seenClassIds.add(id);
                newCount++;
            }
        }

        return newCount; // 你可以根据需要统计探针命中数、类数等
    }

    public int getTotalCoveredProbes() throws Exception {
        IAgent agent = RT.getAgent();
        byte[] execData = agent.getExecutionData(false); // 不清除状态

        ExecutionDataStore eds = new ExecutionDataStore();
        SessionInfoStore sis = new SessionInfoStore();

        try (InputStream in = new ByteArrayInputStream(execData)) {
            ExecutionDataReader reader = new ExecutionDataReader(in);
            reader.setExecutionDataVisitor(eds);
            reader.setSessionInfoVisitor(sis);
            reader.read();
        }

        int coveredProbes = 0;
        for (ExecutionData ed : eds.getContents()) {
            boolean[] probes = ed.getProbes();
            if (probes != null) {
                for (boolean p : probes) {
                    if (p) coveredProbes++;
                }
            }
        }
        return coveredProbes;
    }


    private void startHourlyCoverageLogger() {
        Thread loggerThread = new Thread(() -> {
            while (true) {
                try {
                    int classCount = seenClassIds.size();
                    int probeCount = getTotalCoveredProbes(); // 此处统计的是当前累计覆盖数（不重置）

                    String timestamp = new SimpleDateFormat("yyyyMMdd_HH").format(new Date());
                    String logDir = "coverage-log";
                    Files.createDirectories(Paths.get(logDir));
                    String logFile = logDir + "/coverage_" + timestamp + ".txt";

                    String logEntry = String.format("[%s] Total Classes: %d, Total Covered Probes: %d%n",
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                            classCount, probeCount);

                    Files.write(Paths.get(logFile), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                    IAgent agent = RT.getAgent();
                    byte[] execData = agent.getExecutionData(false); // 不清除探针状态

                    String fileName = logDir + "/coverage_" + timestamp + ".exec";

                    // 写入 .exec 文件（二进制格式）
                    try (OutputStream out = new FileOutputStream(fileName)) {
                        out.write(execData);
                    }

                    Thread.sleep(3600 * 1000); // 每小时睡眠
                } catch (Exception e) {
                    e.printStackTrace(); // 可选：写入错误日志
                }
            }
        });
        loggerThread.setDaemon(true); // 守护线程
        loggerThread.start();
    }
}

