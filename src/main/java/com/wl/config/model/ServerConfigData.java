package com.wl.config.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class ServerConfigData {


    private String url;

    private String host;


    private Optional<Integer> port = Optional.empty();

    private String alias;

    private int connectTimes = 0;

    private Optional<SSHTunnelConfigData> sshTunnelConfig = Optional.empty();

}
