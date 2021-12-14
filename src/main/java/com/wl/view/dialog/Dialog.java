package com.wl.view.dialog;


import com.wl.utils.FXMLs;

public class Dialog {

    private static final DialogController dialogController = FXMLs.getController("fxml/Dialog.fxml");

    public static void confirm(String title,
                               String content,
                               Runnable confirmAction) {
        dialogController.showAndWait(title, content, confirmAction);
    }
}
