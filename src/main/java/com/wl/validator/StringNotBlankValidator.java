package com.wl.validator;

import com.jfoenix.validation.base.ValidatorBase;
import com.wl.utils.StringUtil;
import javafx.scene.control.TextInputControl;

public class StringNotBlankValidator extends ValidatorBase {

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            TextInputControl textField = (TextInputControl) srcControl.get();
            String alias = textField.getText();
            if (alias == null) {
                hasErrors.set(true);
                setMessage("must not be blank");
            } else {
                hasErrors.set(false);
            }
        }
    }
}
