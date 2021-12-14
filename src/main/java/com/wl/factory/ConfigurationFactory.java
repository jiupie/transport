package com.wl.factory;


import com.wl.config.JsonPrettyZooConfigRepository;
import com.wl.config.PrettyZooConfigRepository;
import com.wl.config.entity.Configuration;
import com.wl.config.entity.ServerConfiguration;
import com.wl.config.model.ConfigData;
import com.wl.config.value.SSHTunnelConfiguration;
import com.wl.listener.ConfigurationChangeListener;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ConfigurationFactory {

    private PrettyZooConfigRepository prettyZooConfigRepository = new JsonPrettyZooConfigRepository();

    public Configuration create(List<ConfigurationChangeListener> listeners) {
        ConfigData configData = prettyZooConfigRepository.get();
        final List<ServerConfiguration> serverConfigurations = configData.getServers()
                .stream()
                .map(serverConfig -> {
                    SSHTunnelConfiguration tunnelConfiguration = serverConfig.getSshTunnelConfig()
                            .map(tunnelConfig -> SSHTunnelConfiguration.builder()
                                    .localhost(tunnelConfig.getLocalhost())
                                    .localPort(tunnelConfig.getLocalPort())
                                    .sshUsername(tunnelConfig.getSshUsername())
                                    .sshPassword(tunnelConfig.getPassword())
                                    .sshHost(tunnelConfig.getSshHost())
                                    .sshPort(tunnelConfig.getSshPort())
                                    .remoteHost(tunnelConfig.getRemoteHost())
                                    .remotePort(tunnelConfig.getRemotePort())
                                    .build())
                            .orElse(null);
                    final String[] hostAndPort = serverConfig.getHost().split(":");
                    // compatible: before v1.9.2 host is [xxx:port]
                    final String host = serverConfig.getPort().map(p -> serverConfig.getHost())
                            .orElse(hostAndPort[0]);
                    final Integer port = serverConfig.getPort()
                            .orElseGet(() -> Integer.parseInt(hostAndPort[1]));
                    String url = host + ":" + port;
                    return ServerConfiguration.builder()
                            .alias(serverConfig.getAlias())
                            .url(url)
                            .host(host)
                            .port(port)
                            .connectTimes(serverConfig.getConnectTimes())
//                            .sshTunnelEnabled(serverConfig.getSshTunnelEnabled())
                            .sshTunnel(tunnelConfiguration)
                            .build();
                })
                .collect(Collectors.toList());
        final Configuration.FontConfiguration fontConfiguration = getOrDefaultFontConfiguration(configData.getFontConfig());
        final Locale locale = configData.getLocalConfig().getLang().getLocale();
        final Configuration.LocaleConfiguration localeConfiguration = new Configuration.LocaleConfiguration(locale);
        return Configuration.builder()
                .fontConfiguration(fontConfiguration)
                .localeConfiguration(localeConfiguration)
                .configurationChangeListeners(listeners)
                .serverConfigurations(serverConfigurations)
                .build();
    }

    private Configuration.FontConfiguration getOrDefaultFontConfiguration(ConfigData.FontConfigData data) {
        if (data == null) {
            return new Configuration.FontConfiguration(14);
        } else {
            return new Configuration.FontConfiguration(data.getFontSize());
        }
    }
}
