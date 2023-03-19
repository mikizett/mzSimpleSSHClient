package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.services.events.ConnectSshEvent;
import com.mz.sshclient.services.events.listener.ISshConnectionListener;

public interface ISshConnectionObservableService extends IService {

    void addConnectSshListener(ISshConnectionListener l);

    void fireConnectSshEvent(ConnectSshEvent event);

}
