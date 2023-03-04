package com.mz.sshclient.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SessionFolderModel extends AbstractSessionEntryModel {
    private List<SessionFolderModel> folders = new ArrayList<>(0);
    private List<SessionItemModel> items = new ArrayList<>(0);

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        final SessionFolderModel that = (SessionFolderModel) obj;
        if (id.equals(that.id) && name.equals(that.name) && folders.equals(that.folders) && items.equals(that.items)) {
            return true;
        }
        return false;
    }
}
