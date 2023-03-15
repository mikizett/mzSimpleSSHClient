package com.mz.sshclient.ssh;

import java.io.Serializable;

public interface IClosedSshConnectionCallback {
    void closedSshConnection(Serializable reference);
}
