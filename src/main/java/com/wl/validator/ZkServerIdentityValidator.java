package com.wl.validator;


import com.jfoenix.validation.base.ValidatorBase;
import com.wl.vo.PrettyZooFacade;
import javafx.scene.control.TextInputControl;

public class ZkServerIdentityValidator extends ValidatorBase {

    private PrettyZooFacade facade = new PrettyZooFacade();

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            TextInputControl textField = (TextInputControl) srcControl.get();
            if (textField.isEditable()) {
                final String zkServer = textField.getText();
                if (zkServer == null || facade.hasServerConfiguration(zkServer)) {
                    hasErrors.set(true);
                    setMessage(zkServer + " already exists");
                }
            } else {
                hasErrors.set(false);
            }
        }
    }
}
