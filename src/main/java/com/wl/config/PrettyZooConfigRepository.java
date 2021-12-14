package com.wl.config;


import com.wl.config.model.ConfigData;

import java.io.InputStream;
import java.io.OutputStream;

public interface PrettyZooConfigRepository {


    String CONFIG_PATH = System.getProperty("user.home") + "/.transport/prettyZoo.cfg";

    ConfigData get();

    void save(ConfigData config);

    default void importConfig(InputStream stream) {

    }

    default void exportConfig(OutputStream targetStream) {
    }
}
