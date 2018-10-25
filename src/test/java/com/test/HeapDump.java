package com.test;

import com.sun.management.HotSpotDiagnosticMXBean;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Lazy;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class HeapDump implements TestRule {
    public static final String HEAPDUMP_PATH = "heapdump.path";
    private static final String HOTSPOT_DIAGNOSTICS_BEAN_NAME =
            "com.sun.management:type=HotSpotDiagnostic";

    public HeapDump() {

    }

    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
                heapDump(description.getTestClass());
            }
        };
    }


    private static final Lazy<HotSpotDiagnosticMXBean> diagnosticMXBean =
            new Lazy<>(() ->
            {
                try {
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                    return ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_DIAGNOSTICS_BEAN_NAME, HotSpotDiagnosticMXBean.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            );


    public static void heapDump(Class clazz) {
        heapDump(clazz.getSimpleName());
    }

    public static void heapDump(String qualifier) {
        String dumpLocation = System.getProperty(HEAPDUMP_PATH);
        if (dumpLocation != null && !dumpLocation.isEmpty()) {
            dumpLocation = String.format(dumpLocation, qualifier);
            System.out.println("dumpLocation = " + dumpLocation);
            heapDumpToFile(dumpLocation);
        } else {
            LoggerFactory.getLogger(HeapDump.class).info("Property \"" + HEAPDUMP_PATH + "\"%s is not defined, heap dump skipped");
        }

    }

    public static void heapDumpToFile(String dumpLocation) {
        try {
            System.gc();
            Thread.sleep(300);
            System.gc();
            //noinspection ResultOfMethodCallIgnored
            new File(dumpLocation).delete();
            LoggerFactory.getLogger(HeapDump.class).info("Dumping heap to {}", dumpLocation);
            diagnosticMXBean.get().dumpHeap(dumpLocation, true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
