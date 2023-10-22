package com.mz.sshclient.ui.config;

import com.mz.sshclient.Constants;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;

import java.io.File;

public final class AppConfig {

    private AppConfig() {}

    static {
        final File f = new File(Constants.DEFAULT_STORAGE_LOCATION);
        if (!f.exists() && !f.isFile()) {
            if (!f.mkdir()) {
                final String msg = new StringBuilder("Could not create folder: ")
                        .append(Constants.DEFAULT_STORAGE_PATH_NAME).append(" !\n")
                        .append("Please check the permission !")
                        .toString();

                MessageDisplayUtil.showErrorMessage(null, msg);
                System.exit(-1);
            }
        }
    }

    public static String getStorageLocation() {
        return Constants.DEFAULT_STORAGE_LOCATION;
    }

    public static String getLogFileLocation() {
        return getStorageLocation() + File.separatorChar + Constants.LOG_PATH_NAME;
    }

    public static String getSessionFileLocation() {
        return getStorageLocation() + File.separatorChar + Constants.SESSIONS_LOCATION;
    }

    public static String getKnownHostsLocation() {
        return getStorageLocation() + File.separatorChar + Constants.KNOWN_HOSTS_FILE_LOCATION;
    }

    public static String getPasswordStorageFilePath() {
        return getStorageLocation() + File.separatorChar + Constants.PASSWORD_STORAGE_FILE_LOCATION;
    }

    public static String getPasswordStorageLocation() {
        return getStorageLocation() + File.separatorChar + Constants.PASSWORD_STORAGE_PATH;
    }

}
