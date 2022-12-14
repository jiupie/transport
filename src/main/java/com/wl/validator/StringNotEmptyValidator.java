package com.wl.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.control.TextInputControl;

public class StringNotEmptyValidator extends ValidatorBase {

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            TextInputControl textField = (TextInputControl) srcControl.get();
            String alias = textField.getText();
            if (alias != null && !alias.isEmpty()) {
                hasErrors.set(true);
                setMessage("must not be all blank");
            } else {
                hasErrors.set(false);
           }
        }
    }
}
