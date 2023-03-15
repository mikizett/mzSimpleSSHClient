package com.mz.sshclient.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.model.SessionModel;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.config.AppConfig;
import com.mz.sshclient.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class SessionDataService implements ISessionDataService {

    private static final Logger LOG = LogManager.getLogger(SessionDataService.class);

    private File sessionFile;
    private SessionModel sessionModel;
    private SessionModel defaultSessionModel;

    private SessionItemModel selectedSessionItemModel;

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

    @Override
    public void saveToFile() throws SaveSessionDataException {
        if (hasSessionModelChanged()) {
            synchronized (this) {
                final ObjectMapper objectMapper = new ObjectMapper();
                try {
                    // make sure folder exists
                    checkIfSessionFileLocationExists();
                    objectMapper.writeValue(sessionFile, sessionModel);
                    loadSessionModelFromFile();
                } catch (IOException e) {
                    throw new SaveSessionDataException("Could not save file", e);
                }
            }
        }
    }

    @Override
    public SessionModel getSessionModel() {
        return sessionModel;
    }

    @Override
    public SessionFolderModel createNewSessionFolderModel() {
        return createNewSessionFolderModel(UUID.randomUUID().toString(), "New Folder");
    }

    @Override
    public SessionFolderModel createAndAddNewSessionFolder(final SessionFolderModel parentSessionFolder) {
        final SessionFolderModel folder = createNewSessionFolderModel();
        parentSessionFolder.getFolders().add(folder);

        LOG.info("Created new session folder: " + folder.getName() + " in parent: " + parentSessionFolder.getName());
        return folder;
    }

    @Override
    public SessionFolderModel addSessionFolder(SessionFolderModel parentSessionFolder, SessionFolderModel newSessionFolder) {
        parentSessionFolder.getFolders().add(newSessionFolder);

        LOG.info("Created new session folder: " + newSessionFolder.getName() + " in parent: " + parentSessionFolder.getName());

        return newSessionFolder;
    }

    @Override
    public SessionItemModel addSessionItem(SessionFolderModel parentSessionFolder, SessionItemModel newSessionItem) {
        parentSessionFolder.getItems().add(newSessionItem);

        LOG.info("Created new session item: " + newSessionItem.getName() + " in parent folder: " + parentSessionFolder.getName());

        return newSessionItem;
    }

    @Override
    public void removeSessionItemFrom(final SessionFolderModel parentSessionFolderModel, final SessionItemModel sessionItemModel) {
        parentSessionFolderModel.getItems().remove(sessionItemModel);

        LOG.info("Removed session item: " + sessionItemModel.getName() + " from parent folder: " + parentSessionFolderModel.getName());
    }

    @Override
    public void removeSessionFolderFrom(SessionFolderModel parentSessionFolderModel, SessionFolderModel sessionFolderModel) {
        parentSessionFolderModel.getFolders().remove(sessionFolderModel);

        LOG.info("Removed session folder: " + sessionFolderModel.getName() + " from parent folder: " + parentSessionFolderModel.getName());
    }

    @Override
    public void setSelectedSessionItemModel(final SessionItemModel selectedSessionItemModel) {
        this.selectedSessionItemModel = selectedSessionItemModel;
    }

    @Override
    public SessionItemModel getSelectedSessionItemModel() {
        return selectedSessionItemModel;
    }

    @Override
    public boolean hasSessionModelChanged() {
        return defaultSessionModel != null && sessionModel != null && !defaultSessionModel.equals(sessionModel);
    }

}
