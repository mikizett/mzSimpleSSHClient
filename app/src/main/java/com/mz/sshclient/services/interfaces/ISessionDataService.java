package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.model.session.SessionFolderModel;
import com.mz.sshclient.model.session.SessionItemModel;
import com.mz.sshclient.model.session.SessionModel;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;

import java.util.List;

public interface ISessionDataService extends IService {

    void saveToFile() throws SaveSessionDataException;
    void changedSessionItems(SessionFolderModel folderModel);

    SessionModel getSessionModel();
    SessionModel getDefaultSessionModel();

    SessionFolderModel createNewSessionFolderModel();
    SessionFolderModel createAndAddNewSessionFolder(SessionFolderModel parentSessionFolder);

    SessionFolderModel addSessionFolder(SessionFolderModel parentSessionFolder, SessionFolderModel newSessionFolder);
    SessionFolderModel addSessionFolder(SessionFolderModel parentSessionFolder, SessionFolderModel newSessionFolder, int index);
    SessionItemModel addSessionItem(SessionFolderModel parentSessionFolder, SessionItemModel newSessionItem);
    SessionItemModel addSessionItem(SessionFolderModel parentSessionFolder, SessionItemModel newSessionItem, int index);

    void removeSessionItemFrom(SessionFolderModel parentSessionFolderModel, SessionItemModel sessionItemModel);
    void removeSessionFolderFrom(SessionFolderModel parentSessionFolderModel, SessionFolderModel sessionFolderModel);

    boolean hasSessionModelChanged();

    boolean hasAddedOrModifiedSessionItemModels();
    List<SessionItemModel> getNewAndModifiedSessionItemModels();
    void addNewOrModifiedSessionItemModel(SessionItemModel sessionItemModel);

}
