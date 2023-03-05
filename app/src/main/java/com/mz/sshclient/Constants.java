package com.mz.sshclient;

import java.io.File;

public interface Constants {

    String USER_HOME = System.getProperty("user.home");

    String APP_VERSION = "v0.0.1";
    String APP_NAME = "mzSimpleSSHClient";
    String APP_NAME_AND_VERSION = APP_NAME + " | " + APP_VERSION;

    String SESSIONS_PATH_NAME = "sessions";
    String SESSIONS_FILE_NAME = "sessions.json";
    String SESSIONS_LOCATION = SESSIONS_PATH_NAME + File.separatorChar + SESSIONS_FILE_NAME;

    String DEFAULT_STORAGE_PATH_NAME = "mzSimpleSSHClient";
    String DEFAULT_STORAGE_LOCATION = USER_HOME + File.separatorChar + DEFAULT_STORAGE_PATH_NAME;
    String DEFAULT_SESSIONS_LOCATION = DEFAULT_STORAGE_LOCATION + File.separatorChar + SESSIONS_LOCATION;

    String CONFIG_FILE_LOCATION = USER_HOME + File.separatorChar + ".mzSimpleSSHClient.conf";

    String LOG_PATH_NAME = "log";

}
