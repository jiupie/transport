package com.wl.fp;

@FunctionalInterface
public interface CheckedSupplier<R> {

    R get() throws Exception;

}
