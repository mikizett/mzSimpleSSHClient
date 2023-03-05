package com.mz.sshclient.ui.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ActionSaveSessions extends AbstractAction {
    private static final Logger LOG = LogManager.getLogger(ActionSaveSessions.class);

    //private ISessionDataService sessionDataService = ServiceRegistry.getDefault().get(ISessionDataService.class);

    public ActionSaveSessions(final String title) {
        super(title);
        //sessionDataService.registerSessionDataChangedListener(this);
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        /*try {
            sessionDataService.saveToFile();
            setEnabled(false);
        } catch (SaveSessionDataException ex) {
            LOG.error(ex.getMessage(), ex);
            MessageDisplayUtil.showErrorMessage(SwingUtilities.getWindowAncestor((Component) e.getSource()), ex.getMessage());
        }*/
    }
}
