package com.mz.sshclient.ssh;

import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;

public interface IPasswordRetryCallback {

    char[] readPassword(String user, IPasswordFinderCallback passwordFinderCallback) throws SshOperationCanceledException;

}
