package com.mz.sshclient.ui.actions;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
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
