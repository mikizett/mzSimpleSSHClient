package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.model.SessionModel;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;

public interface ISessionDataService extends IService {

    void saveToFile() throws SaveSessionDataException;
    void changedSessionItems(SessionFolderModel folderModel);

    SessionModel getSessionModel();

    SessionFolderModel createNewSessionFolderModel();
    SessionFolderModel createAndAddNewSessionFolder(SessionFolderModel parentSessionFolder);

    SessionFolderModel addSessionFolder(SessionFolderModel parentSessionFolder, SessionFolderModel newSessionFolder);
    SessionItemModel addSessionItem(SessionFolderModel parentSessionFolder, SessionItemModel newSessionItem);

    void removeSessionItemFrom(SessionFolderModel parentSessionFolderModel, SessionItemModel sessionItemModel);
    void removeSessionFolderFrom(SessionFolderModel parentSessionFolderModel, SessionFolderModel sessionFolderModel);

    boolean hasSessionModelChanged();

}
