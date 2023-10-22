package com.mz.sshclient.model.session;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        if (id.equals(that.id) && name.equals(that.name) &&
                folders.size() == that.folders.size() && folders.equals(that.folders) &&
                items.size() == that.items.size() & items.equals(that.items)) {
            return true;
        }
        return false;
    }

    public SessionFolderModel clone(boolean useNewIdAndCopyAsSessionName) {
        final SessionFolderModel folder = new SessionFolderModel();

        if (useNewIdAndCopyAsSessionName) {
            folder.setId(UUID.randomUUID().toString());
            folder.setName(new StringBuilder(this.name).append(" (Copy)").toString());
        } else {
            folder.setId(this.id);
            folder.setName(this.name);
        }

        final List<SessionFolderModel> cloneSessionFolderList = new ArrayList<>(folders.size());
        folders.forEach(cloneFolder -> cloneSessionFolderList.add(cloneFolder.clone(useNewIdAndCopyAsSessionName)));
        folder.setFolders(cloneSessionFolderList);

        final List<SessionItemModel> cloneSessionItemModelList = new ArrayList<>(items.size());
        items.forEach(item -> cloneSessionItemModelList.add(item.clone(useNewIdAndCopyAsSessionName)));
        folder.setItems(cloneSessionItemModelList);

        return folder;
    }
}
