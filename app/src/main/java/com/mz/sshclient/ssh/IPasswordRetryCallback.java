package com.mz.sshclient.ssh;

import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;

public interface IPasswordRetryCallback {

    char[] getEncodedPassword(String user) throws SshOperationCanceledException;

}
