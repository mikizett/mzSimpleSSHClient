package com.mz.sshclient.ui.utils;

import com.mz.sshclient.mzSimpleSshClientMain;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Window;

public final class MessageDisplayUtil {

    private MessageDisplayUtil() {}

    public static void showErrorMessage(final String errorMessage) {
        JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showErrorMessage(final Window parent, final String errorMessage) {
        JOptionPane.showMessageDialog(parent, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showMessage(final Window parent, final String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static int showYesNoConfirmDialog(final String message, final String title) {
        return JOptionPane.showConfirmDialog(mzSimpleSshClientMain.MAIN_FRAME, message, title, JOptionPane.YES_NO_OPTION);
    }

    public static int showYesNoConfirmDialog(final Window parent, final String message, final String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
    }

}
