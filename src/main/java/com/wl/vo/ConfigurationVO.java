package com.wl.vo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConfigurationVO {

    private ObservableList<ServerConfigurationVO> servers = FXCollections.observableArrayList();

    public ObservableList<ServerConfigurationVO> getServers() {
        return servers;
    }

    public void setServers(ObservableList<ServerConfigurationVO> servers) {
        this.servers = servers;
    }
}
