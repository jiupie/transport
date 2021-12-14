package com.wl.context;

import javafx.stage.Stage;

public class PrimaryStageContext {

    private static Stage stage;

    public static void set(Stage stage) {
        PrimaryStageContext.stage = stage;
    }

    public static Stage get() {
        return stage;
    }

}
