package com.mz.sshclient.model.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
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

    @Override
    public int hashCode() {
        return Objects.hash(host, port, user, password, privateKeyFile, localFolder, remoteFolder, jumpHost);
    }

    public SessionItemModel clone(boolean useNewIdAndCopyAsSessionName) {
        final SessionItemModel copy = new SessionItemModel();

        if (useNewIdAndCopyAsSessionName) {
            copy.id = UUID.randomUUID().toString();
            copy.name = new StringBuilder(this.name).append(" (Copy)").toString();
        } else {
            copy.id = this.id;
            copy.name = this.name;
        }

        copy.host = this.host;
        copy.port = this.port;
        copy.user = this.user;
        copy.password = this.password;
        copy.privateKeyFile = this.privateKeyFile;
        copy.localFolder = this.localFolder;
        copy.remoteFolder = this.remoteFolder;
        copy.jumpHost = this.jumpHost;

        return copy;
    }

    public <T extends SessionItemModel> void copyFrom(T model) {
        id = model.id;
        name = model.name;
        host = model.getHost();
        port = model.getPort();
        user = model.getUser();
        password = model.getPassword();
        privateKeyFile = model.getPrivateKeyFile();
        localFolder = model.getLocalFolder();
        remoteFolder = model.getRemoteFolder();
        jumpHost = model.getJumpHost();
    }

}
