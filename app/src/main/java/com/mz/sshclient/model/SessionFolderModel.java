package com.mz.sshclient.model;

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

    public SessionFolderModel deepCopy() {
        final SessionFolderModel folder = new SessionFolderModel();

        folder.setId(UUID.randomUUID().toString());
        folder.setName(this.name + " (Copy)");
        folder.setFolders(new ArrayList<>(this.folders));

        final List<SessionItemModel> copySessionItemModelList = new ArrayList<>(this.items.size());
        this.items.forEach(item -> copySessionItemModelList.add(item.deepCopy()));
        folder.setItems(copySessionItemModelList);
        //folder.setItems(new ArrayList<>(this.items));

        return folder;
    }
}
