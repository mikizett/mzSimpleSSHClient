package com.mz.sshclient.ui.components.session.panels.add_or_edit;

import com.mz.sshclient.model.SessionItemDraftModel;
import com.mz.sshclient.ui.events.listener.IValueChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

public class SessionInfoPanel extends JPanel {

    private ConnectionPanel connectionPanel;
    private SecureFtpPanel secureFtpPanel;
    private JumpHostPanel jumpHostPanel;
    private final SessionItemDraftModel sessionItemDraftModel;

    private final AddOrEditEnum addOrEditEnum;

    private final IValueChangeListener changeValueListener;

    public SessionInfoPanel(
            final SessionItemDraftModel sessionItemDraftModel,
            final AddOrEditEnum addOrEditEnum,
            final IValueChangeListener changeValueListener
    ) {
        this.sessionItemDraftModel = sessionItemDraftModel;
        this.addOrEditEnum = addOrEditEnum;
        this.changeValueListener = changeValueListener;

        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        connectionPanel = new ConnectionPanel(sessionItemDraftModel, addOrEditEnum, changeValueListener);
        secureFtpPanel = new SecureFtpPanel(sessionItemDraftModel, addOrEditEnum);
        jumpHostPanel = new JumpHostPanel(sessionItemDraftModel, addOrEditEnum);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Connection", connectionPanel);
        tabs.addTab("sFTP Directories", secureFtpPanel);
        tabs.addTab("Jump Host", jumpHostPanel);

        this.add(tabs);
    }

    public ConnectionPanel getConnectionPanel() {
        return connectionPanel;
    }

    public SecureFtpPanel getSecureFtpPanel() {
        return secureFtpPanel;
    }

    public JumpHostPanel getJumpHostPanel() {
        return jumpHostPanel;
    }

}
