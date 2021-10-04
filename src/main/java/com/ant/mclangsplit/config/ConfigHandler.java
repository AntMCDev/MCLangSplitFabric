package com.ant.mclangsplit.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigHandler {
    public static class Client {
        private static final String CONFIG_DIR = "config/";
        private static final String MOD_CONFIG_DIR = "mclangsplit-client.toml";

        public static final String SECOND_LANGUAGE;
        public static final ArrayList<String> IGNORE_KEYS;
        public static final ArrayList<String> INCLUDE_KEYS;
        public static final Boolean IGNORE_TOOLTIPS;

        static {
            File configDir = new File(CONFIG_DIR);
            if (!configDir.exists()) {
                boolean success = configDir.mkdir();
                if (!success) {
                    throw new RuntimeException("Error creating the config directory");
                }
            }

            Toml toml = new Toml();
            File f = new File(MinecraftClient.getInstance().runDirectory, CONFIG_DIR + MOD_CONFIG_DIR);
            boolean change = false;
            Map<String, Object> map = new HashMap<>();

            if (f.exists()) {
                toml.read(f);
                map = toml.toMap();
            } else {
                try {
                    boolean success = f.createNewFile();
                    if (!success) {
                        throw new RuntimeException("Error creating the " + MOD_CONFIG_DIR + " config file");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }

            if (!map.containsKey("Language Settings")) {
                if (map.containsKey("\"Language Settings\"")) {
                    map.put("Language Settings",  (HashMap<String, Object>)map.get("\"Language Settings\""));
                    map.remove("\"Language Settings\"");
                } else {
                    map.put("Language Settings", new HashMap<String, Object>());
                }
                change = true;
            }

            Map<String, Object> languageSettings = (HashMap<String, Object>)map.get("Language Settings");

            if (!languageSettings.containsKey("secondlanguage")) {
                languageSettings.put("secondlanguage", "");
                change = true;
            }

            if (!languageSettings.containsKey("ignoreKeys")) {
                languageSettings.put("ignoreKeys", new ArrayList<String>());
                change = true;
            }

            if (!languageSettings.containsKey("includeKeys")) {
                languageSettings.put("includeKeys", new ArrayList<String>());
                change = true;
            }

            if (!languageSettings.containsKey("ignoreTooltips")) {
                languageSettings.put("ignoreTooltips", false);
                change = true;
            }

            SECOND_LANGUAGE = (String)languageSettings.get("secondlanguage");
            IGNORE_KEYS = (ArrayList<String>)languageSettings.get("ignoreKeys");
            INCLUDE_KEYS = (ArrayList<String>)languageSettings.get("includeKeys");
            IGNORE_TOOLTIPS = (Boolean)languageSettings.get("ignoreTooltips");

            if (change) {
                TomlWriter writer = new TomlWriter.Builder()
                        .indentValuesBy(0)
                        .indentTablesBy(4)
                        .padArrayDelimitersBy(1)
                        .build();

                try {
                    writer.write(map, f);
                } catch (IOException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }


        }
    }
}
