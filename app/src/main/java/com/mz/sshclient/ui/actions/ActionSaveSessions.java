package com.mz.sshclient.ui.actions;

import javax.swing.JButton;
import java.awt.event.ActionEvent;

public class ActionSaveSessions extends AbstractSaveSessionsAction {

    public ActionSaveSessions() {
        super("Save changes");
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (hasChanged()) {
            saveToFile();
        }

        if (e.getSource() instanceof JButton) {
            ((JButton) e.getSource()).setEnabled(false);
        }
    }

}
