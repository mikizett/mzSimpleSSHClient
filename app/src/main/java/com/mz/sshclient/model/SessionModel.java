package com.mz.sshclient.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionModel {

    private SessionFolderModel folder;

    @Override
    public String toString() {
        return new StringBuilder("Session=").append(folder.toString()).toString();
    }

    @Override
    public boolean equals(Object obj) {
        final SessionModel that = (SessionModel) obj;
        if (folder.equals(that.folder)) {
            return true;
        }
        return false;
    }

}
