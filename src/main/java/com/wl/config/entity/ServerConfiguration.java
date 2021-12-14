package com.wl.config.entity;


import com.wl.config.value.SSHTunnelConfiguration;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServerConfiguration {

    /**
     * host:port
     */
    private String url;

    private String host;

    private Integer port;

    @Builder.Default
    private String alias = "";


    private int connectTimes;

    @Builder.Default
    private Boolean sshTunnelEnabled = false;

    private SSHTunnelConfiguration sshTunnel;

    public void update(ServerConfiguration serverConfiguration) {
        if (serverConfiguration.getSshTunnelEnabled() && serverConfiguration.getSshTunnel() == null) {
            throw new IllegalStateException();
        }
        this.sshTunnelEnabled = serverConfiguration.getSshTunnelEnabled();
        this.sshTunnel = serverConfiguration.getSshTunnel();
        this.alias = serverConfiguration.getAlias();
    }

    public void incrementConnectTimes() {
        this.connectTimes += 1;
    }

}
