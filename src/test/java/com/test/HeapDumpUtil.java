package com.test;

import com.sun.management.HotSpotDiagnosticMXBean;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class HeapDumpUtil {
    private static final String HOTSPOT_BEAN_NAME =
            "com.sun.management:type=HotSpotDiagnostic";
    private static HotSpotDiagnosticMXBean diagnosticMXBean;

    static {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            diagnosticMXBean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void heapDump(Class clazz) {
        heapDump(clazz.getSimpleName());
    }
    public static void heapDump(String qualifier) {
        String dumpLocation = System.getProperty("heapdump.path");
        if (dumpLocation != null && !dumpLocation.isEmpty()) {
            dumpLocation = String.format(dumpLocation, qualifier);
            System.out.println("dumpLocation = " + dumpLocation);
            heapDumpToFile(dumpLocation);
        }
    }

    public static void heapDumpToFile(String dumpLocation) {
        try {
            System.gc();
            Thread.sleep(300);
            System.gc();
            //noinspection ResultOfMethodCallIgnored
            new File(dumpLocation).delete();
            diagnosticMXBean.dumpHeap(dumpLocation, true);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
