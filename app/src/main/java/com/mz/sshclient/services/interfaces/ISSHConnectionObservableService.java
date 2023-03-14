package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.services.events.ConnectSshEvent;
import com.mz.sshclient.services.events.listener.ISshConnectListener;

public interface ISSHConnectionObservableService extends IService {

    void addConnectSshListener(ISshConnectListener l);

    void fireConnectSSHEvent(ConnectSshEvent event);

}
