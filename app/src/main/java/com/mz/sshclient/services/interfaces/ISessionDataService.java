package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionModel;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;

public interface ISessionDataService extends IService {

    void saveToFile() throws SaveSessionDataException;

    SessionModel getSessionModel();

    SessionFolderModel addSessionFolderModel(SessionFolderModel parentSessionFolderModel);

    boolean hasSessionModelChanged();

}
