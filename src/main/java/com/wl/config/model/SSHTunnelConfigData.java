package com.wl.config.model;

import lombok.Data;

@Data
public class SSHTunnelConfigData {

    private String localhost;

    private Integer localPort;

    private String sshHost;

    private Integer sshPort;

    private String remoteHost;

    private Integer remotePort;

    private String sshUsername;

    private String password;

}
