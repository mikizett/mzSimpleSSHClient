package com.mz.sshclient.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mz.sshclient.Constants;
import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionModel;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.config.ConfigFile;
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

    public SessionDataService() {
        final String sessionsFileLocation = ConfigFile.getStorageLocation() + File.separatorChar + Constants.SESSIONS_LOCATION;
        sessionFile = new File(sessionsFileLocation);
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

    private SessionFolderModel createNewSessionFolderModel() {
        return createNewSessionFolderModel(UUID.randomUUID().toString(), "New Folder");
    }

    private SessionFolderModel createNewSessionFolderModel(final String id, final String name) {
        final SessionFolderModel model = new SessionFolderModel();
        model.setId(id);
        model.setName(name);
        return model;
    }

    @Override
    public void saveToFile() throws SaveSessionDataException {
        if (hasSessionModelChanged()) {
            synchronized (this) {
                final ObjectMapper objectMapper = new ObjectMapper();
                try {
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
    public SessionFolderModel addSessionFolderModel(final SessionFolderModel parentSessionFolderModel) {
        final SessionFolderModel folder = createNewSessionFolderModel();
        parentSessionFolderModel.getFolders().add(folder);
        return folder;
    }

    @Override
    public boolean hasSessionModelChanged() {
        return defaultSessionModel != null && sessionModel != null && !defaultSessionModel.equals(sessionModel);
    }

}
