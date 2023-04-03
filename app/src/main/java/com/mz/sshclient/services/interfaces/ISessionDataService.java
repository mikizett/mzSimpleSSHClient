package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.model.SessionModel;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;

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

}
