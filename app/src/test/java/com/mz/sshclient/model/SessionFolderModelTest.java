package com.mz.sshclient.model;

import com.mz.sshclient.model.session.SessionFolderModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SessionFolderModelTest {

    @Test
    void notEqualsTest() {
        final SessionFolderModel m1 = TestData.createSessionFolderModel("1", 3, 5);
        final SessionFolderModel m2 = TestData.createSessionFolderModel("2", 5, 5);

        Assertions.assertFalse(m1.equals(m2));
    }

    @Test
    void equalsTest() {
        final SessionFolderModel m1 = TestData.createSessionFolderModel("1", 3, 5);
        final SessionFolderModel m2 = TestData.createSessionFolderModel("1", 3, 5);

        Assertions.assertTrue(m1.equals(m2));
    }

    @Test
    void cloneWithNewIdAndCopyAsSessionNameTest() {
        final SessionFolderModel m1 = TestData.createSessionFolderModel("1", 3, 5);
        final SessionFolderModel m2 = m1.clone(true);

        Assertions.assertFalse(m1.equals(m2));
        Assertions.assertEquals("name-1 (Copy)", m2.getName());
    }

    @Test
    void cloneTest() {
        final SessionFolderModel m1 = TestData.createSessionFolderModel("1", 3, 5);
        final SessionFolderModel m2 = m1.clone(false);

        Assertions.assertTrue(m1.equals(m2));
        Assertions.assertEquals("name-1", m2.getName());
    }

}
