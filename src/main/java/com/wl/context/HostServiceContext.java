package com.wl.context;

import javafx.application.HostServices;
import javafx.application.Platform;

public class HostServiceContext {

    private static volatile HostServices hostServices = null;

    public static synchronized void set(HostServices service) {
        if (hostServices == null) {
            hostServices = service;
        }
    }

    public static HostServices get() {
        return hostServices;
    }

    public static void jumpToReleases() {
        Platform.runLater(() -> {
            hostServices.showDocument("https://github.com/vran-dev/PrettyZoo/releases");
        });
    }
}
