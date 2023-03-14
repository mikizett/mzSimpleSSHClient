package com.mz.sshclient.ssh;

import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.interfaces.ISSHConnection;
import com.mz.sshclient.services.interfaces.ISessionDataService;

public class SSHConnection implements ISSHConnection {

    final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);

    @Override
    public void connect() {

    }
}
