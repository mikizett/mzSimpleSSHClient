package com.mz.sshclient.ui.actions;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ui.OpenedSshSessions;
import com.mz.sshclient.ui.utils.AWTInvokerUtils;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.event.ActionEvent;

public class ActionExitApp extends AbstractSaveSessionsAction implements QuitHandler {

    private final JFrame frame;

    public ActionExitApp(final JFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        exit(null);
    }

    @Override
    public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
        exit(response);
    }

    private void exit(QuitResponse response) {
        if (hasChanged()) {
            int result = MessageDisplayUtil.showYesNoConfirmDialog(
                    mzSimpleSshClientMain.MAIN_FRAME,
                    "Do you want to save the created session folders?",
                    "Save..."
            );
            if (result == JOptionPane.YES_OPTION) {
                saveToFile();
            }
        }

        // close all opened ssh sessions
        AWTInvokerUtils.invokeLater(() -> {
            if (OpenedSshSessions.hasOpenedSessions()) {
                int answer = MessageDisplayUtil.showYesNoConfirmDialog(
                        "Do you want to close all opened sessions?",
                        "Close opened sessions..."
                );
                if (answer == JOptionPane.YES_OPTION) {
                    OpenedSshSessions.closeAllSshSessions();
                    if (response != null) {
                        closeForMac(response, answer);
                    } else {
                        close();
                    }
                } else {
                    if (response != null) {
                        closeForMac(response, answer);
                    }
                }
            } else {
                if (response != null) {
                    closeForMac(response, JOptionPane.YES_OPTION);
                } else {
                    close();
                }
            }
        });
    }

    private void close() {
        if (frame != null) {
            frame.dispose();
            frame.setVisible(false);
            System.exit(0);
        }
    }

    private void closeForMac(QuitResponse response, int answer) {
        if (answer == JOptionPane.YES_OPTION) {
            response.performQuit();
        } else {
            response.cancelQuit();
        }
    }

}
