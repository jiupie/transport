package com.wl.service;


import com.wl.config.entity.SSHTunnel;
import com.wl.config.entity.ServerConfiguration;
import com.wl.config.value.SSHTunnelConfiguration;
import com.wl.listener.ServerListener;
import com.wl.view.toast.VToast;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SSHTunnelService {
    private static final Map<String, SSHTunnel> SSHMap = new ConcurrentHashMap<>();

    public SSHTunnel connect(ServerConfiguration serverConfig, List<ServerListener> serverListeners) {
        if(Objects.nonNull(SSHMap.get(serverConfig.getUrl()))){
            VToast.error(serverConfig.getUrl()+"already connected");
            return null;
        }
        SSHTunnel tunnel = null;
        if (serverConfig.getSshTunnel() != null) {
            final SSHTunnelConfiguration tunnelConfig = serverConfig.getSshTunnel();
            tunnel = SSHTunnel.builder()
                    .localhost(tunnelConfig.getLocalhost())
                    .localPort(tunnelConfig.getLocalPort())
                    .sshUsername(tunnelConfig.getSshUsername())
                    .sshPassword(tunnelConfig.getSshPassword())
                    .sshHost(tunnelConfig.getSshHost())
                    .sshPort(tunnelConfig.getSshPort())
                    .remoteHost(tunnelConfig.getRemoteHost())
                    .remotePort(tunnelConfig.getRemotePort())
                    .build();
        }
        if (Objects.nonNull(tunnel)) {
            tunnel.createAsync();
            tunnel.blockUntilConnected();
            SSHMap.put(serverConfig.getUrl(), tunnel);
            return tunnel;
        } else {
            return null;
        }
    }


    public void disconnect(String url) {
        if (SSHMap.containsKey(url)) {
            SSHMap.get(url).close();
            SSHMap.remove(url);
        }
    }

    public void disconnectAll() {
        SSHMap.values().forEach(SSHTunnel::close);
        SSHMap.clear();
    }
}
