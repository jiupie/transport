package com.wl.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleUtils {

    public static ResourceBundle get(Locale locale) {
        return ResourceBundle.getBundle("i18n.lang", locale);
    }

}
