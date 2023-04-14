package com.mz.sshclient.ui.components.session.panels;

import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.actions.ActionSaveSessions;
import com.mz.sshclient.ui.components.common.tree.SessionTreeComponent;
import com.mz.sshclient.ui.components.session.popup.SessionActionsPopupMenu;
import com.mz.sshclient.ui.events.listener.ITreeNodeListener;
import com.mz.sshclient.ui.utils.AWTInvokerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class SessionOverviewPanel extends JPanel implements ITreeNodeListener {

    private static final Logger LOG = LogManager.getLogger(SessionOverviewPanel.class);

    private final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);

    private final JToggleButton popupButton = new JToggleButton("Actions");
    private final JButton saveButton = new JButton(new ActionSaveSessions());

    private SessionTreeComponent sessionTreeComponent;

    private SessionActionsPopupMenu popupMenu;

    public SessionOverviewPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        add(createActionsPanel(), BorderLayout.NORTH);

        // load the session tree component in a separate thread to have the main frame loaded to show the master
        // password dialog
        AWTInvokerUtils.invokeInSeparateThread(() -> {
            sessionTreeComponent = new SessionTreeComponent();
            sessionTreeComponent.addTreeNodeListener(this);

            add(new JScrollPane(sessionTreeComponent));

            sessionTreeComponent.revalidate();
            revalidate();
        });
    }

    private JPanel createActionsPanel() {
        final JPanel panel = new JPanel(new BorderLayout());

        final JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));

        popupButton.addActionListener(l -> {
            if (popupMenu != null) {
                popupButton.setSelected(false);
                popupMenu.setVisible(false);
                popupMenu = null;
            } else {
                popupMenu = createSessionActionsPopupMenu();
                popupMenu.show(popupButton, 5, popupButton.getHeight() + 1);
            }
        });

        north.add(popupButton);
        north.add(saveButton);

        panel.add(north, BorderLayout.NORTH);

        return panel;
    }

    private SessionActionsPopupMenu createSessionActionsPopupMenu() {
        final SessionActionsPopupMenu popupMenu = new SessionActionsPopupMenu(sessionTreeComponent) {
            @Override
            protected void firePopupMenuWillBecomeInvisible() {
                super.firePopupMenuWillBecomeInvisible();
                popupButton.setSelected(false);
            }
        };
        popupMenu.setPreferredSize(new Dimension(popupButton.getPreferredSize().width + 50, popupMenu.getPreferredSize().height));
        return popupMenu;
    }

    @Override
    public void treeNodeAction() {
        saveButton.setEnabled(sessionDataService.hasSessionModelChanged());
    }
}

