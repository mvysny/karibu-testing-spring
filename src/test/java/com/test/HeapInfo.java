package com.test;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public class HeapInfo extends HashMap<String, HeapInfo.ClassHeapInfo> {
    private final static ObjectName diagCmdBeanName;
    private static final String DIAGNOSTICS_COMMAND_BEAN_NAME =
            "com.sun.management:type=DiagnosticCommand";
    private static byte[] buffer;


    static {
        try {
            diagCmdBeanName = new ObjectName(DIAGNOSTICS_COMMAND_BEAN_NAME);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Pattern CSV_PATTERN = Pattern.compile(",");

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class ClassHeapInfo {

        private String className;
        private long instSize;
        private long instCount;
        private long instBytes;

        public ClassHeapInfo() {
        }

        public ClassHeapInfo(String className, long instSize, long instCount, long instBytes) {
            this.className = className;
            this.instSize = instSize;
            this.instCount = instCount;
            this.instBytes = instBytes;
        }

        @Override
        public String toString() {
            return "{" +
                    "size:" + instSize +
                    ", count:" + instCount +
                    ", total bytes:" + instBytes +
                    '}';
        }

        public String getClassName() {
            return className;
        }

        public long getInstSize() {
            return instSize;
        }

        public long getInstCount() {
            return instCount;
        }

        public long getInstBytes() {
            return instBytes;
        }

        protected void setClassName(String classname) {
            this.className = classname;
        }

        protected void setInstSize(int instSize) {
            this.instSize = instSize;
        }

        protected void setInstCount(int instCount) {
            this.instCount = instCount;
        }

        protected void setInstBytes(int instBytes) {
            this.instBytes = instBytes;
        }

        public ClassHeapInfo negate() {
            return new ClassHeapInfo(className, instSize, -instCount, -instBytes);
        }

        public ClassHeapInfo sub(ClassHeapInfo anotherInfo) {
            return new ClassHeapInfo(className,
                    instSize,
                    instCount - anotherInfo.instCount,
                    instBytes - anotherInfo.instBytes);
        }

        public boolean isNotEmpty() {
            return instBytes != 0 || instCount != 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClassHeapInfo)) return false;
            ClassHeapInfo that = (ClassHeapInfo) o;
            return Objects.equals(className, that.className);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className);
        }
    }

    public HeapInfo classStatistics(Predicate<String> filter) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            String data = (String) server.invoke(diagCmdBeanName, "gcClassStats", new Object[]{new String[]{"-csv", "columns:ClassName,InstSize,InstCount,InstBytes"}}, new String[]{String[].class.getName()});
            BufferedReader reader = new BufferedReader(new StringReader(data));
            String header = reader.readLine();
            if (!header.contains("ClassName")) {
                throw new RuntimeException("Something went wong with JMX beans: " + data);
            }
            String[] headers = CSV_PATTERN.split(header);
            Predicate<String> myFilter = s -> !s.startsWith(HeapInfo.class.getName());
            if (filter != null) {
                myFilter = myFilter.and(filter);
            }
            for (String l; (l = reader.readLine()) != null; ) {
                String[] stats = CSV_PATTERN.split(l);
                ClassHeapInfo info = new ClassHeapInfo();
                for (int i = 0; i < headers.length; i++) {
                    if (headers.length <= stats.length) {

                        switch (headers[i]) {
                            case "ClassName":
                                if (myFilter.test(stats[i])) {
                                    info.setClassName(stats[i]);
                                    put(info.getClassName(), info);
                                }
                                break;
                            case "InstSize":
                                info.instSize = Long.parseLong(stats[i].trim());
                                break;
                            case "InstCount":
                                info.instCount = Long.parseLong(stats[i].trim());
                                break;
                            case "InstBytes":
                                info.instBytes = Long.parseLong(stats[i].trim());
                                break;
                        }
                    }

                }
            }
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void printNicely(Predicate<ClassHeapInfo> filter, Comparator<ClassHeapInfo> sorter, Consumer<String> out) {
        Stream<ClassHeapInfo> stream = values().stream();
        if (filter != null) {
            stream = stream.filter(filter);
        }
        if (sorter == null) {
            sorter = Comparator.comparingLong(HeapInfo.ClassHeapInfo::getInstBytes).reversed();
        }
        stream = stream.sorted(sorter);
        stream.map(info -> info.getClassName() + ":" + info)
                .forEach(out);
    }

    public HeapInfo delta(HeapInfo compare) {
        Map<String, ClassHeapInfo> copy = new TreeMap<>(this);
        HeapInfo result = new HeapInfo();
        for (ClassHeapInfo anotherInfo : compare.values()) {
            ClassHeapInfo heapInfo = copy.remove(anotherInfo.getClassName());
            ClassHeapInfo deltaHeapInfo;
            if (heapInfo == null) {
                deltaHeapInfo = anotherInfo.negate();
            } else {
                deltaHeapInfo = heapInfo.sub(anotherInfo);
            }
            if (deltaHeapInfo.isNotEmpty()) {
                result.put(deltaHeapInfo.getClassName(), deltaHeapInfo);
            }
        }
        copy.values().stream()
                .filter(ClassHeapInfo::isNotEmpty)
                .forEach(value -> result.put(value.getClassName(), value));
        return result;
    }

    public static void main(String[] args) throws InterruptedException {
        HeapInfo heapInfo = new HeapInfo();
        HeapInfo heapInfo2 = new HeapInfo();
        System.gc();
        Thread.sleep(200);
        System.gc();
        heapInfo.classStatistics(s -> true);
        buffer = new byte[1000000];
        System.gc();
        Thread.sleep(200);
        System.gc();
        heapInfo2.classStatistics(s -> true);
        heapInfo2.delta(heapInfo)
                .printNicely(info -> info.getInstBytes() > 1000, null, System.out::println);
    }

    public static void tryGC() {
        for (int i = 0; i < 5; i++) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {

            }
        }
    }
}
