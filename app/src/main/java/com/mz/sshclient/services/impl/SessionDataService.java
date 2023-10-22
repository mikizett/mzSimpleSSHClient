package com.mz.sshclient.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mz.sshclient.model.session.SessionFolderModel;
import com.mz.sshclient.model.session.SessionItemModel;
import com.mz.sshclient.model.session.SessionModel;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.config.AppConfig;
import com.mz.sshclient.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SessionDataService implements ISessionDataService {

    private static final Logger LOG = LogManager.getLogger(SessionDataService.class);

    private File sessionFile;
    private SessionModel sessionModel;
    private SessionModel defaultSessionModel;

    private final List<SessionItemModel> addedOrModifiedSessionItemModels = new ArrayList<>(0);


    public SessionDataService() {
        sessionFile = new File(AppConfig.getSessionFileLocation());
        loadSessionModelFromFile();
    }

    private void loadSessionModelFromFile() {
        final ObjectMapper objectMapper = Utils.createObjectMapper();
        try {
            sessionModel = objectMapper.readValue(sessionFile, new TypeReference<>() {});
            defaultSessionModel = objectMapper.readValue(sessionFile, new TypeReference<>() {});
        } catch (IOException e) {
            LOG.warn("Could not read sessions from file <" + sessionFile.getName() + ">", e);
            LOG.info("Create an empty sessions file <" + sessionFile.getName() + ">");

            sessionModel = new SessionModel();
            defaultSessionModel = new SessionModel();

            final SessionFolderModel rootFolderModel = createNewSessionFolderModel("1", "Sessions");
            final SessionFolderModel rootDefaultFolderModel = createNewSessionFolderModel("1", "Sessions");

            sessionModel.setFolder(rootFolderModel);
            defaultSessionModel.setFolder(rootDefaultFolderModel);
        }
    }

    private SessionFolderModel createNewSessionFolderModel(final String id, final String name) {
        final SessionFolderModel model = new SessionFolderModel();
        model.setId(id);
        model.setName(name);
        return model;
    }

    private void checkIfSessionFileLocationExists() {
        if (!sessionFile.exists()) {
            final File f = sessionFile.getParentFile();
            f.mkdirs();
        }
    }

    private void changeDefaultSessionItem(final SessionFolderModel folderModel, final String id, final String passwd) {
        folderModel.getItems().forEach((item) -> {
            if (item.getId().equals(id)) {
                item.setPassword(passwd);
            }
        });

        folderModel.getFolders().forEach((folder) -> changeDefaultSessionItem(folder, id, passwd));
    }

    @Override
    public void saveToFile() throws SaveSessionDataException {
        if (hasSessionModelChanged()) {
            synchronized (this) {
                final ObjectMapper objectMapper = new ObjectMapper();
                try {
                    // make sure folder exists
                    checkIfSessionFileLocationExists();
                    objectMapper.writeValue(sessionFile, sessionModel);
                    defaultSessionModel.setFolder(sessionModel.getFolder().clone(false));
                    addedOrModifiedSessionItemModels.clear();
                } catch (IOException e) {
                    throw new SaveSessionDataException("Could not save file", e);
                }
            }
        }
    }

    @Override
    public void changedSessionItems(final SessionFolderModel folderModel) {
        if (folderModel != null) {
            folderModel.getItems().forEach((item) -> {
                if (StringUtils.isNotBlank(item.getPassword())) {
                    final String id = item.getId();
                    changeDefaultSessionItem(defaultSessionModel.getFolder(), id, item.getPassword());

                }
            });

            folderModel.getFolders().forEach((folder) -> changedSessionItems(folder));
        }
    }

    @Override
    public SessionModel getSessionModel() {
        return sessionModel;
    }

    @Override
    public SessionModel getDefaultSessionModel() {
        return defaultSessionModel;
    }

    @Override
    public SessionFolderModel createNewSessionFolderModel() {
        return createNewSessionFolderModel(UUID.randomUUID().toString(), "New Folder");
    }

    @Override
    public SessionFolderModel createAndAddNewSessionFolder(final SessionFolderModel parentSessionFolder) {
        final SessionFolderModel folder = createNewSessionFolderModel();
        parentSessionFolder.getFolders().add(folder);

        LOG.debug("Created new session folder: " + folder.getName() + " in parent: " + parentSessionFolder.getName());

        return folder;
    }

    @Override
    public SessionFolderModel addSessionFolder(SessionFolderModel parentSessionFolder, SessionFolderModel newSessionFolder) {
        return addSessionFolder(parentSessionFolder, newSessionFolder, -1);
    }

    @Override
    public SessionFolderModel addSessionFolder(SessionFolderModel parentSessionFolder, SessionFolderModel newSessionFolder, int index) {
        if (index > -1) {
            parentSessionFolder.getFolders().add(index, newSessionFolder);
        } else {
            parentSessionFolder.getFolders().add(newSessionFolder);
        }

        LOG.debug("Created new session folder: " + newSessionFolder.getName() + " in parent: " + parentSessionFolder.getName());

        return newSessionFolder;
    }

    @Override
    public SessionItemModel addSessionItem(SessionFolderModel parentSessionFolder, SessionItemModel newSessionItem) {
        return addSessionItem(parentSessionFolder, newSessionItem, -1);
    }

    @Override
    public SessionItemModel addSessionItem(SessionFolderModel parentSessionFolder, SessionItemModel newSessionItem, int index) {
        if (index > -1) {
            parentSessionFolder.getItems().add(index, newSessionItem);
        } else {
            parentSessionFolder.getItems().add(newSessionItem);
        }

        LOG.debug("Created new session item: " + newSessionItem.getName() + " in parent folder: " + parentSessionFolder.getName());

        return newSessionItem;
    }

    @Override
    public void removeSessionItemFrom(final SessionFolderModel parentSessionFolderModel, final SessionItemModel sessionItemModel) {
        parentSessionFolderModel.getItems().remove(sessionItemModel);

        LOG.debug("Removed session item: " + sessionItemModel.getName() + " from parent folder: " + parentSessionFolderModel.getName());
    }

    @Override
    public void removeSessionFolderFrom(SessionFolderModel parentSessionFolderModel, SessionFolderModel sessionFolderModel) {
        parentSessionFolderModel.getFolders().remove(sessionFolderModel);

        LOG.debug("Removed session folder: " + sessionFolderModel.getName() + " from parent folder: " + parentSessionFolderModel.getName());
    }

    @Override
    public boolean hasSessionModelChanged() {
        return defaultSessionModel != null && sessionModel != null && !defaultSessionModel.equals(sessionModel);
    }

    @Override
    public boolean hasAddedOrModifiedSessionItemModels() {
        return !addedOrModifiedSessionItemModels.isEmpty();
    }

    @Override
    public List<SessionItemModel> getNewAndModifiedSessionItemModels() {
        return addedOrModifiedSessionItemModels;
    }

    @Override
    public void addNewOrModifiedSessionItemModel(SessionItemModel sessionItemModel) {
        if (!addedOrModifiedSessionItemModels.contains(sessionItemModel)) {
            addedOrModifiedSessionItemModels.add(sessionItemModel);
        }
    }

}
