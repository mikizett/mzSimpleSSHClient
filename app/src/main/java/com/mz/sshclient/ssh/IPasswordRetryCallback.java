package com.mz.sshclient.ssh;

public interface IPasswordRetryCallback {

    char[] readPassword(String user, IPasswordFinderCallback passwordFinderCallback);

}
