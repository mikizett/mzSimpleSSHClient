package com.mz.sshclient.model;

import com.mz.sshclient.model.session.SessionFolderModel;
import com.mz.sshclient.model.session.SessionItemDraftModel;
import com.mz.sshclient.model.session.SessionItemModel;

import java.util.ArrayList;
import java.util.List;

public class TestData {

    public static SessionItemModel createSessionItemModel(String s) {
        final SessionItemModel m = new SessionItemModel();

        m.setId("1-" + s);
        m.setName("name-" + s);
        m.setUser("user-" + s);
        m.setHost("host-" + s);
        m.setPort("port-" + s);
        m.setPassword("passwd-" + s);
        m.setPrivateKeyFile("privateKey-" + s);
        m.setLocalFolder("localFolder-" + s);
        m.setRemoteFolder("remoteFolder-" + s);
        m.setJumpHost("jumpHost-" + s);

        return m;
    }

    public static SessionItemDraftModel createSessionItemDraftModel(String s) {
        final SessionItemDraftModel m = new SessionItemDraftModel();

        m.setId("1-" + s);
        m.setName("name-" + s);
        m.setUser("user-" + s);
        m.setHost("host-" + s);
        m.setPort("port-" + s);
        m.setPassword("passwd-" + s);
        m.setPrivateKeyFile("privateKey-" + s);
        m.setLocalFolder("localFolder-" + s);
        m.setRemoteFolder("remoteFolder-" + s);
        m.setJumpHost("jumpHost-" + s);

        return m;
    }

    public static SessionFolderModel createSessionFolderModel(String s, int folderSize, int itemSize) {
        final SessionFolderModel m = new SessionFolderModel();

        m.setId("id-" + s);
        m.setName("name-" + s);

        final List<SessionFolderModel> sessionFolderModelList = new ArrayList<>(0);

        if (folderSize > 0) {
            for (int i = 0; i < folderSize; i++) {
                final SessionFolderModel create = new SessionFolderModel();
                create.setId("" + i+1);
                create.setName("name" + i+1);

                sessionFolderModelList.add(create);
            }
        }

        m.setFolders(sessionFolderModelList);

        final List<SessionItemModel> sessionItemModelList = new ArrayList<>(0);

        if (itemSize > 0) {
            for (int i = 0; i < itemSize; i++) {
                sessionItemModelList.add(createSessionItemModel("" + i+1));
            }
        }

        m.setItems(sessionItemModelList);

        return m;
    }

}
