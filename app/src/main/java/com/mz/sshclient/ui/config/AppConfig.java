package com.mz.sshclient.ui.config;

import com.mz.sshclient.Constants;

import java.io.File;

public final class AppConfig {

    private AppConfig() {}

    public static String getStorageLocation() {
        return ConfigFile.getStorageLocation();
    }

    public static String getLogFileLocation() {
        return getStorageLocation() + File.separatorChar + Constants.LOG_PATH_NAME;
    }

    public static String getSessionFileLocation() {
        return getStorageLocation() + File.separatorChar + Constants.SESSIONS_LOCATION;
    }

    public static String getKnownHostsLocation() {
        return getStorageLocation() + File.separatorChar + Constants.KNOWN_HOSTS_FILE_NAME;
    }
}
