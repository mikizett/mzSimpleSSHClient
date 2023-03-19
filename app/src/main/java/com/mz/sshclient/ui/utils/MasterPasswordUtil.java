package com.mz.sshclient.ui.utils;

import com.mz.sshclient.mzSimpleSshClientMain;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public final class MasterPasswordUtil {

    private MasterPasswordUtil() {}

    public static MasterPasswordAnswer showMasterPasswordDialog(final String title) {
        final JPasswordField passwordField = new JPasswordField(30);
        UIUtils.addAncestorAndFocusListenerToPasswordField(passwordField);

        int result = JOptionPane.showOptionDialog(
                mzSimpleSshClientMain.MAIN_FRAME,
                new Object[] {
                        title,
                        passwordField
                },
                "Master password",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null);

        return new MasterPasswordAnswer(result, passwordField.getPassword());
    }

    public static String getMessageToStoreSessionsWithMasterPassword() {
        return new StringBuilder("Please add a master password to store sessions with passwords.\n\n")
                .append("This password will be used\n").append("to unlock the password storage file.\n\n")
                .append("If you forget the master password the password storage can't be used!\n")
                .append("In this case you have to add the passwords again!\n\n")
                .append("The passwords are BASE64 encoded and\n").append("the password storage file is AES encrypted.\n")
                .append(" ")
                .toString();
    }

    public static String getMessageToReadSessionsWithMasterPassword() {
        return new StringBuilder("Please add the master password\n")
                .append("to unlock the password storage file.\n").append(" ")
                .toString();
    }

    @AllArgsConstructor
    @Getter
    public static final class MasterPasswordAnswer {
        private int answerType;
        private char[] password;
    }

}
