package com.mz.sshclient.ui.components.tab;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.events.ConnectSSHEvent;
import com.mz.sshclient.services.events.listener.IConnectSSHListener;
import com.mz.sshclient.services.interfaces.ISSHConnectionService;
import com.mz.sshclient.ui.components.common.tabbedpane.CustomTabbedPaneClosable;

import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;

public class TabContainerPanel extends JPanel implements IConnectSSHListener {

    private final ISSHConnectionService sshConnectionService = ServiceRegistry.get(ISSHConnectionService.class);

    private CustomTabbedPaneClosable tabbedPane;

    public TabContainerPanel() {
        init();
        sshConnectionService.addConnectSSHListener(this);
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY));

        tabbedPane = new CustomTabbedPaneClosable();

        /*for (int i = 1; i <= 30; i++) {
            tabbedPane.addTab("Shell-" + i, new TabContentPanel());
        }*/

        add(tabbedPane);
    }

    @Override
    public void connectSSH(ConnectSSHEvent event) {
        final SessionItemModel item = event.getSessionItemModel();
        tabbedPane.addTab(item.getName(), new TabContentPanel());
        tabbedPane.toggleTabLayoutPolicy();
    }
}
