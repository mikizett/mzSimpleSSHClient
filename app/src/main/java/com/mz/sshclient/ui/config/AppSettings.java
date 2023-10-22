package com.mz.sshclient.ui.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mz.sshclient.Constants;
import com.mz.sshclient.model.appsettings.AppSettingsModel;
import com.mz.sshclient.utils.Utils;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public final class AppSettings {
    private AppSettings() {}

    private static final File APP_SETTINGS_FILE = new File(Constants.APP_SETTINGS_LOCATION);

    private static final Logger LOG = LogManager.getLogger(AppSettings.class);

    private static final boolean DEFAULT_DARK_MODE = true;

    @Getter
    private static boolean darkMode = DEFAULT_DARK_MODE;

    @Getter
    private static String[] jumpHosts = new String[] { "" };

    @Getter
    private static String selectedJumpHost = "";

    public static void init() {
        if (APP_SETTINGS_FILE.exists() && APP_SETTINGS_FILE.isFile()) {
            loadAppSettingsFile();
        }
    }

    public static void saveToFile(AppSettingsModel model) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            // make sure folder exists
            checkIfSessionFileLocationExists();
            objectMapper.writeValue(APP_SETTINGS_FILE, model);

            jumpHosts = model.getJumpHosts();
            selectedJumpHost = model.getSelectedJumpHost();
            darkMode = model.isDarkMode();
        } catch (IOException e) {
            LOG.error("Could not save to file: " + APP_SETTINGS_FILE.getName(), e);
            throw e;
        }
    }

    private static void loadAppSettingsFile() {
        final ObjectMapper objectMapper = Utils.createObjectMapper();
        try {
            AppSettingsModel appSettingsModel = objectMapper.readValue(APP_SETTINGS_FILE, new TypeReference<>() {});

            jumpHosts = appSettingsModel.getJumpHosts();
            selectedJumpHost = appSettingsModel.getSelectedJumpHost();
            darkMode = appSettingsModel.isDarkMode();
        } catch (IOException e) {
            LOG.error("Could not read app settings file: ", e);
        }
    }

    private static void checkIfSessionFileLocationExists() {
        if (!APP_SETTINGS_FILE.exists()) {
            final File f = APP_SETTINGS_FILE.getParentFile();
            f.mkdirs();
        }
    }
}
