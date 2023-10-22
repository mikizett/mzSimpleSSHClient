package com.mz.sshclient.ui.actions;

import com.mz.sshclient.model.session.SessionItemModel;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.components.tabs.terminal.PasswordStorageHandler;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import java.util.List;

public abstract class AbstractSaveSessionsAction extends AbstractAction {

    private static final Logger LOG = LogManager.getLogger(AbstractSaveSessionsAction.class);

    private final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);

    public AbstractSaveSessionsAction() {}

    public AbstractSaveSessionsAction(final String actionName) {
        super(actionName);
    }

    protected boolean hasChanged() {
        return sessionDataService.hasSessionModelChanged() && sessionDataService.hasAddedOrModifiedSessionItemModels();
    }

    protected void saveToFile() {
        final List<SessionItemModel> addedOrModifiedSessionItemModels = sessionDataService.getNewAndModifiedSessionItemModels();
        addedOrModifiedSessionItemModels.forEach(item -> PasswordStorageHandler.getHandler().storePassword(item));

        try {
            sessionDataService.saveToFile();
        } catch (SaveSessionDataException ex) {
            LOG.error(ex);
            MessageDisplayUtil.showErrorMessage(ex.getMessage());
        }
    }
}
