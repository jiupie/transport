package com.wl.vo;

import com.wl.config.entity.Configuration;
import com.wl.config.entity.ServerConfiguration;
import com.wl.config.model.ConfigData;
import com.wl.config.value.SSHTunnelConfiguration;
import com.wl.listener.ConfigurationChangeListener;
import com.wl.listener.ServerListener;
import com.wl.service.ConfigurationDomainService;
import com.wl.service.SSHTunnelService;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PrettyZooFacade {

    private static final Logger log = LoggerFactory.getLogger(PrettyZooFacade.class);

    private static final ConfigurationDomainService configurationDomainService = new ConfigurationDomainService();
    private static final SSHTunnelService sshTunnelService = new SSHTunnelService();
     /**
     * 保存配置
     *
     * @param serverConfigurationVO
     */
    public void saveServerConfiguration(ServerConfigurationVO serverConfigurationVO) {
        SSHTunnelConfiguration.SSHTunnelConfigurationBuilder tunnelConfigurationBuilder = SSHTunnelConfiguration.builder();
        if (serverConfigurationVO.getRemoteServer().trim().length() > 0) {
            tunnelConfigurationBuilder.remoteHost(serverConfigurationVO.getRemoteServer())
                    .remotePort(serverConfigurationVO.getRemoteServerPort());
        }

        if (serverConfigurationVO.getSshServer().trim().length() > 0) {
            tunnelConfigurationBuilder.sshHost(serverConfigurationVO.getSshServer())
                    .sshPort(serverConfigurationVO.getSshServerPort());
        }
        tunnelConfigurationBuilder.localhost(serverConfigurationVO.getZkHost())
                .localPort(serverConfigurationVO.getZkPort());
        tunnelConfigurationBuilder.sshUsername(serverConfigurationVO.getSshUsername())
                .sshPassword(serverConfigurationVO.getSshPassword());

        ServerConfiguration serverConfiguration = ServerConfiguration.builder()
                .alias(serverConfigurationVO.getZkAlias())
                .url(serverConfigurationVO.getZkUrl())
                .host(serverConfigurationVO.getZkHost())
                .port(serverConfigurationVO.getZkPort())
                .sshTunnelEnabled(serverConfigurationVO.isSshEnabled())
                .sshTunnel(tunnelConfigurationBuilder.build())
                .build();
        configurationDomainService.save(serverConfiguration);
    }

    public Integer getFontSize() {
        return configurationDomainService.get().orElseThrow(()->new RuntimeException("fonSize error")).getFontConfiguration().getSize();
    }

    public void changeFontSize(Integer newSize) {
        configurationDomainService.save(new Configuration.FontConfiguration(newSize));
    }


    public boolean hasServerConfiguration(String host) {
        return configurationDomainService.containServerConfig(host);
    }

    public void deleteServerConfiguration(String server) {
        configurationDomainService.deleteServerConfiguration(server);
    }

    public Locale getLocale() {
        return configurationDomainService.getLocale();
    }

    public void updateLocale(ConfigData.Lang lang) {
        configurationDomainService.save(new Configuration.LocaleConfiguration(lang.getLocale()));
    }

    public List<ServerConfigurationVO> loadServerConfigurations(ConfigurationChangeListener changeListener) {
        final Configuration configuration = configurationDomainService.load(Arrays.asList(changeListener));
        return configuration.getServerConfigurations()
                .stream()
                .map(ConfigurationVOTransfer::to)
                .collect(Collectors.toList());
    }

    public List<ServerConfigurationVO> getServerConfigurations() {
        final Configuration configuration = configurationDomainService.get().orElseThrow(()->new RuntimeException("configuration service"));
        return configuration.getServerConfigurations()
                .stream()
                .map(ConfigurationVOTransfer::to)
                .collect(Collectors.toList());
    }

    public void exportConfig(File file) {
        Objects.requireNonNull(file);
        configurationDomainService.exportConfig(file);
    }

    public void importConfig(File configFile) {
        if (Objects.isNull(configFile)) {
            log.error("文件不存在");
            return;
        }
        if (!configFile.isFile()) {
            log.error("请选择文件");
            return;
        }
        configurationDomainService.importConfig(configFile);
    }


    public CompletableFuture<Void> connect(String url,
                                           List<ServerListener> serverListeners) {
        return CompletableFuture.runAsync(() -> {
            ServerConfiguration serverConfig = configurationDomainService.get(url).orElseThrow(()->new RuntimeException("connect error"));
            sshTunnelService.connect(serverConfig, serverListeners);
            configurationDomainService.incrementConnectTimes(url);
        });
    }

    public void disconnect(String host) {
        Platform.runLater(() -> {
            sshTunnelService.disconnect(host);
        });
    }

}
