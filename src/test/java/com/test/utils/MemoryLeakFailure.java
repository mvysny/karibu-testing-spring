package com.test.utils;


public class MemoryLeakFailure extends AssertionError {

    public MemoryLeakFailure() {
    }

    public MemoryLeakFailure(Object detailMessage) {
        super(detailMessage);
    }

    public MemoryLeakFailure(boolean detailMessage) {
        super(detailMessage);
    }

    public MemoryLeakFailure(char detailMessage) {
        super(detailMessage);
    }

    public MemoryLeakFailure(int detailMessage) {
        super(detailMessage);
    }

    public MemoryLeakFailure(long detailMessage) {
        super(detailMessage);
    }

    public MemoryLeakFailure(float detailMessage) {
        super(detailMessage);
    }

    public MemoryLeakFailure(double detailMessage) {
        super(detailMessage);
    }

    public MemoryLeakFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
