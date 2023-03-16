package com.mz.sshclient.ui.actions;

import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.event.ActionEvent;

public class ActionSaveSessions extends AbstractAction {

    private static final Logger LOG = LogManager.getLogger(ActionSaveSessions.class);

    private final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);

    public ActionSaveSessions(final String title) {
        super(title);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            sessionDataService.saveToFile();
            if (e.getSource() instanceof JButton) {
                ((JButton) e.getSource()).setEnabled(false);
            }
        } catch (SaveSessionDataException ex) {
            LOG.error(ex);
            MessageDisplayUtil.showErrorMessage(SwingUtilities.getWindowAncestor((Component) e.getSource()), ex.getMessage());
        }
    }

}
