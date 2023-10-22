package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.model.session.SessionFolderModel;
import com.mz.sshclient.model.session.SessionItemModel;
import com.mz.sshclient.services.exceptions.PasswordStorageException;

public interface IPasswordStorageService extends IService {

    boolean existStorageFile();
    void setPasswordsToModel(SessionFolderModel model);
    void storePassword(SessionItemModel sessionItemModel) throws PasswordStorageException;

    void unlockPasswordStorage(char[] masterPassword) throws PasswordStorageException;
    void addMasterPassword(char[] masterPassword) throws PasswordStorageException;

    boolean isUnlockedPasswordStorage();

}
