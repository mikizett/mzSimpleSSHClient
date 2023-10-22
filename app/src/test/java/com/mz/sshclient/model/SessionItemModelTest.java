package com.mz.sshclient.model;

import com.mz.sshclient.model.session.SessionItemDraftModel;
import com.mz.sshclient.model.session.SessionItemModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SessionItemModelTest {

    @Test
    void notEqualsTest() {
        final SessionItemModel m1 = TestData.createSessionItemModel("1");
        final SessionItemModel m2 = TestData.createSessionItemModel("2");
        Assertions.assertFalse(m1.equals(m2));
    }

    @Test
    void equalsTest() {
        final SessionItemModel m1 = TestData.createSessionItemModel("1");
        final SessionItemModel m2 = TestData.createSessionItemModel("1");

        Assertions.assertTrue(m1.equals(m2));
    }

    @Test
    void cloneWithNewIdAndCopyAsSessionNameTest() {
        final SessionItemModel m1 = TestData.createSessionItemModel("1");
        final SessionItemModel m2 = m1.clone(true);

        Assertions.assertFalse(m1.equals(m2));
        Assertions.assertTrue(m2.getName().equals("name-1 (Copy)"));
    }

    @Test
    void cloneTest() {
        final SessionItemModel m1 = TestData.createSessionItemModel("1");
        final SessionItemModel m2 = m1.clone(false);

        Assertions.assertTrue(m1.equals(m2));
        Assertions.assertTrue(m2.getName().equals("name-1"));
    }

    @Test
    void copyFromTest() {
        final SessionItemDraftModel copyFrom = TestData.createSessionItemDraftModel("1");

        final SessionItemModel copyTo = new SessionItemModel();
        copyTo.copyFrom(copyFrom);

        Assertions.assertTrue(copyFrom.equals(copyTo));
        Assertions.assertTrue(copyTo.getName().equals("name-1"));
    }
}
