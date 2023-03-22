package com.mz.sshclient.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SessionItemModel extends AbstractSessionEntryModel {
    private String host = "";
    private String port = "";
    private String user = "";

    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    private String password = "";

    private String privateKeyFile = "";
    private String localFolder = "";
    private String remoteFolder = "";
    private String jumpHost = "";

    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonIgnore))
    private int index = -1;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        final SessionItemModel that = (SessionItemModel) obj;
        if (id.equals(that.id) && name.equals(that.name) && host.equals(that.host) && port.equals(that.port)
                && user.equals(that.user) && password.equals(that.password) && privateKeyFile.equals(that.privateKeyFile) && localFolder.equals(that.localFolder)
                && remoteFolder.equals(that.remoteFolder) && jumpHost.equals(that.jumpHost)
        ) {
            return true;
        }

        return false;
    }

    public SessionItemModel deepCopy() {
        final SessionItemModel copy = new SessionItemModel();

        copy.id = new String(UUID.randomUUID().toString());
        copy.name = new String(this.name + " (Copy)");
        copy.host = new String(this.host);
        copy.port = new String(this.port);
        copy.user = new String(this.user);
        copy.password = new String(this.password);
        copy.privateKeyFile = new String(this.privateKeyFile);
        copy.localFolder = new String(this.localFolder);
        copy.remoteFolder = new String(this.remoteFolder);
        copy.jumpHost = new String(this.jumpHost);

        return copy;
    }

    public <T extends SessionItemModel> void deepCopyFrom(T model) {
        id = new String(model.id);
        name = new String(model.name);
        host = new String(model.getHost());
        port = new String(model.getPort());
        user = new String(model.getUser());
        password = new String(model.getPassword());
        privateKeyFile = new String(model.getPrivateKeyFile());
        localFolder = new String(model.getLocalFolder());
        remoteFolder = new String(model.getRemoteFolder());
        jumpHost = new String(model.getJumpHost());
    }
}
