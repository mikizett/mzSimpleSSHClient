package com.mz.sshclient.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionItemModel extends AbstractSessionEntryModel {
    private String host = "";
    private String port = "";
    private String user = "";
    private String privateKeyFile = "";
    private String localFolder = "";
    private String remoteFolder = "";
    private String jumpHost = "";

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        final SessionItemModel that = (SessionItemModel) obj;
        if (id.equals(that.id) && name.equals(that.name) && host.equals(that.host) && port.equals(that.port)
                && user.equals(that.user) && privateKeyFile.equals(that.privateKeyFile) && localFolder.equals(that.localFolder)
                && remoteFolder.equals(that.remoteFolder) && jumpHost.equals(that.jumpHost)
        ) {
            return true;
        }

        return false;
    }
}
