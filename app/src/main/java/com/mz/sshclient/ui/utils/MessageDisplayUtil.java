package com.mz.sshclient.ui.utils;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Window;

public final class MessageDisplayUtil {

    private MessageDisplayUtil() {}

    public static void showErrorMessage(final String errorMessage) {
        JOptionPane.showMessageDialog(new JFrame(), errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showErrorMessage(final Window parent, final String errorMessage) {
        JOptionPane.showMessageDialog(parent, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static int showYesNoConfirmDialog(final String message, final String title) {
        return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
    }

    public static int showYesNoConfirmDialog(final Window parent, final String message, final String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
    }

}
