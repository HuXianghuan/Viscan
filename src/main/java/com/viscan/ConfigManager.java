package com.viscan;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Path CONFIG_FILE = Path.of(System.getProperty("user.home"), ".viscan.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    private static BioConfig config;


    public static BioConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public static void loadConfig() {
        if (Files.exists(CONFIG_FILE)) {
            try {
                config = mapper.readValue(CONFIG_FILE.toFile(), BioConfig.class);

            } catch (IOException e) {
                e.printStackTrace();
                config = new BioConfig(); //load err, use default
            }
        } else {
            config = new BioConfig(); //file not exist, use default
        }
    }

    public static void saveConfig() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(CONFIG_FILE.toFile(), config);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
