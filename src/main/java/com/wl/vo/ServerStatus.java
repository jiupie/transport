package com.wl.vo;

public enum ServerStatus {

    CONNECTED,

    DISCONNECTED,

    CONNECTING,

    RECONNECTING;

    public boolean isConnecting() {
        return this == CONNECTING || this == RECONNECTING;
    }
}
