package com.mz.sshclient.services.events.listener;

import com.mz.sshclient.services.events.ConnectSshEvent;

public interface ISshConnectionListener {
    void connectSsh(ConnectSshEvent event);

}
