package com.wl.service;



import com.wl.config.JsonPrettyZooConfigRepository;
import com.wl.config.PrettyZooConfigRepository;
import com.wl.config.entity.Configuration;
import com.wl.config.entity.ServerConfiguration;
import com.wl.factory.ConfigurationFactory;
import com.wl.listener.ConfigurationChangeListener;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Slf4j
public class ConfigurationDomainService {

    private static final Cache<Configuration> configurationCache = new Cache<>();

    private PrettyZooConfigRepository prettyZooConfigRepository = new JsonPrettyZooConfigRepository();

    public Configuration load(List<ConfigurationChangeListener> listeners) {
        final Configuration configuration = new ConfigurationFactory().create(listeners);
        configurationCache.setVal(configuration);
        prettyZooConfigRepository.save(configuration.toPersistModel());
        return configuration;
    }

    public void save(ServerConfiguration serverConfig) {
        final Configuration configuration = get().orElseThrow(()->new RuntimeException(""));
        final Optional<ServerConfiguration> serverConfigurationOpt = get(serverConfig.getUrl());
        if (serverConfigurationOpt.isPresent()) {
            configuration.update(serverConfig);
        } else {
            configuration.add(serverConfig);
        }
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public void save(Configuration.FontConfiguration fontConfiguration) {
        fontConfiguration.checkIsValid();
        Configuration configuration = get().orElseThrow(()->new RuntimeException(""));
        configuration.updateFont(fontConfiguration);
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public void save(Configuration.LocaleConfiguration localeConfiguration) {
        Objects.requireNonNull(localeConfiguration);
        Configuration configuration = get().orElseThrow(()->new RuntimeException(""));
        configuration.updateLocale(localeConfiguration);
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public Optional<Configuration> get() {
        return Optional.ofNullable(configurationCache.getVal());
    }

    public Optional<ServerConfiguration> get(String url) {
        return get().orElseThrow(()->new RuntimeException())
                .getServerConfigurations()
                .stream()
                .filter(s -> s.getUrl().equals(url))
                .findFirst();
    }

    public Locale getLocale() {
        final Configuration configuration = new ConfigurationFactory().create(Arrays.asList());
        return configuration.getLocaleConfiguration().getLocale();
    }

    public void deleteServerConfiguration(String server) {
        Objects.requireNonNull(configurationCache.getVal());
        configurationCache.getVal().delete(server);
        prettyZooConfigRepository.save(configurationCache.getVal().toPersistModel());
    }




    public Boolean containServerConfig(String server) {
        Objects.requireNonNull(configurationCache.getVal());
        return configurationCache.getVal().exists(server);
    }

    public void importConfig(File configFile) {
        try (InputStream stream = Files.newInputStream(configFile.toPath(), StandardOpenOption.READ)) {
            prettyZooConfigRepository.importConfig(stream);
            final Configuration originConfiguration = configurationCache.getVal();
            load(originConfiguration.getConfigurationChangeListeners());
        } catch (Exception e) {
            log.error("import config failed", e);
            throw new IllegalStateException("import config failed", e);
        }
    }

    public void exportConfig(File dir) {
        try (OutputStream stream = Files.newOutputStream(dir.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            prettyZooConfigRepository.exportConfig(stream);
        } catch (IOException e) {
            log.error("export config failed", e);
            throw new IllegalStateException("export config failed", e);
        }
    }

    public void incrementConnectTimes(String server) {
        get().ifPresent(config -> {
            config.incrementConnectTimes(server);
            prettyZooConfigRepository.save(config.toPersistModel());
        });
    }

    private static class Cache<T> {

        private T val;

        public T getVal() {
            return val;
        }

        public void setVal(T val) {
            this.val = val;
        }

    }
}
