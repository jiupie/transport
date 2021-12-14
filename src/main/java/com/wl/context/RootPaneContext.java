package com.wl.context;

import javafx.scene.layout.StackPane;

public class RootPaneContext {

    private static volatile StackPane root;

    public static StackPane get() {
        return root;
    }

    public static void set(StackPane pane) {
        root = pane;
    }
}
