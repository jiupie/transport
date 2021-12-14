package com.wl.listener;

public interface ServerListener {

    default void onConnected(String serverHost) {

    }

    default void onClose(String serverHost) {

    }

    default void onReconnecting(String serverHost) {

    }
}
