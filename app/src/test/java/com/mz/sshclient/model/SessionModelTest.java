package com.mz.sshclient.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SessionModelTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static SessionModel sessionModel;

    @BeforeAll
    public static void beforeAll() throws IOException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final URL url = SessionModelTest.class.getResource("/sessions.json");
        sessionModel = objectMapper.readValue(url, new TypeReference<>() {});
    }

    @Test
    void sessionModelTest() {
        // given
        final SessionFolderModel folderModel = sessionModel.getFolder();

        // when
        List<SessionFolderModel> folders = folderModel.getFolders();
        List<SessionItemModel> items = folderModel.getItems();

        // then
        Assertions.assertEquals(folders.size(), 1);
        Assertions.assertEquals(items.size(), 0);

        // main folder
        SessionFolderModel folder_0 = folders.get(0);
        Assertions.assertEquals(folder_0.getId(), "1a");
        Assertions.assertEquals(folder_0.getName(), "session-1a");
        Assertions.assertEquals(folder_0.getFolders().size(), 1);
        Assertions.assertEquals(folder_0.getItems().size(), 0);

        List<SessionFolderModel> foldersAt_0 = folder_0.getFolders();
        Assertions.assertEquals(foldersAt_0.size(), 1);

        SessionFolderModel folderFolder_0 = foldersAt_0.get(0);
        Assertions.assertEquals(folderFolder_0.getId(), "1b");
        Assertions.assertEquals(folderFolder_0.getName(), "session-1b");

        List<SessionFolderModel> folderFolderFolders = folderFolder_0.getFolders();
        Assertions.assertEquals(folderFolderFolders.size(), 1);

        items = folderFolderFolders.get(0).getItems();
        Assertions.assertEquals(items.size(), 1);

        SessionItemModel item_0 = items.get(0);
        Assertions.assertEquals(item_0.getId(), "item-1");
        Assertions.assertEquals(item_0.getName(), "ITEM-1");
        Assertions.assertEquals(item_0.getHost(), "host-1");
        Assertions.assertEquals(item_0.getPort(), "22");
        Assertions.assertEquals(item_0.getUser(), "user-1");
    }

}
