package com.mz.sshclient.ui.actions;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ssh.exceptions.SshDisconnectException;
import com.mz.sshclient.ui.components.common.tabbedpane.IClosableHeaderTabComponent;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

public class ActionCloseSshTab extends AbstractAction {

    private final SshTtyConnector sshTtyConnector;

    public ActionCloseSshTab(final SshTtyConnector sshTtyConnector) {
        this.sshTtyConnector = sshTtyConnector;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (sshTtyConnector.isConnected() && sshTtyConnector.isRunning()) {
            final int answer = MessageDisplayUtil.showYesNoConfirmDialog(mzSimpleSshClientMain.MAIN_FRAME, "Do you want to close the ssh session?", "Question");
            if (answer == JOptionPane.YES_OPTION) {
                sshTtyConnector.close();
            }
        }
    }

}
