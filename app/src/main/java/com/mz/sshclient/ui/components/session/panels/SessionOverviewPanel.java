package com.mz.sshclient.ui.components.session.panels;

import com.mz.sshclient.ui.actions.ActionSaveSessions;
import com.mz.sshclient.ui.components.common.tree.SessionTreeComponent;
import com.mz.sshclient.ui.components.session.popup.SessionActionsPopupMenu;
import com.mz.sshclient.ui.events.listener.ISessionDataChangedListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class SessionOverviewPanel extends JPanel implements ISessionDataChangedListener {

    private JButton saveButton;

    private SessionTreeComponent sessionTreeComponent;

    private final JToggleButton popupButton = new JToggleButton("Actions");

    private SessionActionsPopupMenu popupMenu;

    public SessionOverviewPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        add(createActionsPanel(), BorderLayout.NORTH);

        sessionTreeComponent = new SessionTreeComponent();
        sessionTreeComponent.addSessionDataChangedListener(this);
        add(new JScrollPane(sessionTreeComponent));
    }

    private JPanel createActionsPanel() {
        final JPanel panel = new JPanel(new BorderLayout());

        final JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));

        popupButton.addActionListener(l -> {
            if (popupButton.isSelected()) {
                popupMenu = createSessionActionsPopupMenu();
                popupMenu.show(popupButton, 5, popupButton.getHeight() + 1);
            } else {
                popupButton.setSelected(false);
                popupMenu.setVisible(false);
            }
        });
        north.add(popupButton);

        saveButton = new JButton(new ActionSaveSessions("Save Sessions"));
        saveButton.setEnabled(false);
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
    public void sessionDataChanged() {
        saveButton.setEnabled(true);
    }
}

