package com.wl.fp;

@FunctionalInterface
public interface CheckedRunnable {

    void run() throws Exception;

}
