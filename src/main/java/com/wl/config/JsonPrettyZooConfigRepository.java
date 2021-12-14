package com.wl.config;


import com.wl.config.model.ConfigData;
import com.wl.config.model.ServerConfigData;
import com.wl.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonPrettyZooConfigRepository implements PrettyZooConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonPrettyZooConfigRepository.class);

    @Override
    public ConfigData get() {
        return doLoad();
    }

    private ConfigData doLoad() {
        final ConfigData config = JsonUtils.from(CONFIG_PATH, ConfigData.class);
        final List<ServerConfigData> sortedServers = config.getServers()
                .stream()
                .sorted(Comparator.comparingInt(ServerConfigData::getConnectTimes))
                .collect(Collectors.toList());
        // sort by connect times desc
        Collections.reverse(sortedServers);
        config.setServers(sortedServers);
        return config;
    }


    @Override
    public void save(ConfigData config) {
        try {
            final String json = JsonUtils.to(config);
            Files.write(Paths.get(CONFIG_PATH), json.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void importConfig(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                String s = null;
                if ((s = reader.readLine()) != null) {
                    builder.append(s);
                } else {
                    break;
                }
            } catch (IOException e) {
                logger.error("import config failed", e);
                throw new RuntimeException("import config failed, cause " + e.getMessage());
            }
        }
        String jsonConfig = builder.toString();
        ConfigData newConfig = JsonUtils.fromJson(jsonConfig, ConfigData.class);
        merge(get(), newConfig);
    }

    private void merge(ConfigData originConfig, ConfigData newConfig) {
        boolean conflict = newConfig.getServers().stream().anyMatch(s -> s.getUrl() == null);
        if (conflict) {
            throw new IllegalStateException("Import failed, because imported config is out-of-date");
        }
        // ignore exists server
        Set<String> originServers = originConfig.getServers()
                .stream()
                .map(ServerConfigData::getUrl)
                .collect(Collectors.toSet());
        List<ServerConfigData> newServers = newConfig.getServers()
                .stream()
                .filter(server -> !originServers.contains(server.getUrl()))
                .collect(Collectors.toList());

        // add new Server
        originConfig.getServers().addAll(newServers);

        // serialize
        save(originConfig);
    }

    @Override
    public void exportConfig(OutputStream targetStream) {
        try {
            final ConfigData config = get();
            final String json = JsonUtils.to(config);
            targetStream.write(json.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
