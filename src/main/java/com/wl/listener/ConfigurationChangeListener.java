package com.wl.listener;


import com.wl.config.model.ServerConfigData;

import java.util.List;

public interface ConfigurationChangeListener {

    default void onServerAdd(ServerConfigData serverConfig) {

    }

    default void onServerRemove(ServerConfigData serverConfig) {

    }

    default void onServerChange(ServerConfigData newValue) {

    }

    default void onReload(List<ServerConfigData> configs) {

    }

}
