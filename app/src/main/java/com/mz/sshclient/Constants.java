package com.mz.sshclient;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public interface Constants {

    String USER_HOME = SystemUtils.USER_HOME;

    String APP_VERSION = "v0.0.1-ALPHA";
    String APP_NAME = "mzSimpleSSHClient";
    String APP_NAME_AND_VERSION = APP_NAME + " | " + APP_VERSION;

    String SESSIONS_PATH_NAME = "sessions";
    String SESSIONS_FILE_NAME = "sessions.json";
    String SESSIONS_LOCATION = SESSIONS_PATH_NAME + File.separatorChar + SESSIONS_FILE_NAME;

    String KNOWN_HOSTS_FILE_NAME = "known_hosts";
    String KNOWN_HOSTS_FILE_LOCATION = SESSIONS_PATH_NAME + File.separatorChar + KNOWN_HOSTS_FILE_NAME;

    String PASSWORD_STORAGE_FILE_NAME = "passwords.storage";
    String PASSWORD_STORAGE_FILE_LOCATION = SESSIONS_PATH_NAME + File.separatorChar + PASSWORD_STORAGE_FILE_NAME;

    String DEFAULT_STORAGE_PATH_NAME = "mzSimpleSSHClient";
    String DEFAULT_STORAGE_LOCATION = USER_HOME + File.separatorChar + DEFAULT_STORAGE_PATH_NAME;

    String CONFIG_FILE_LOCATION = USER_HOME + File.separatorChar + ".mzSimpleSSHClient.conf";

    String LOG_PATH_NAME = "log";

}
