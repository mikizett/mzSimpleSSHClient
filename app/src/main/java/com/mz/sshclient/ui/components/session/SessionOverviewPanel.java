package com.mz.sshclient.ui.components.session;

import com.mz.sshclient.ui.components.common.tree.SessionTreeComponent;
import com.mz.sshclient.ui.components.session.popup.SessionActionsPopupMenu;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class SessionOverviewPanel extends JPanel {

    private SessionTreeComponent sessionTreeComponent = new SessionTreeComponent();

    private final JToggleButton popupButton = new JToggleButton("Actions");

    private SessionActionsPopupMenu popupMenu;

    public SessionOverviewPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        add(createActionsPanel(), BorderLayout.NORTH);

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

        JButton saveButton = new JButton("Save Sessions"/*new ActionSaveSessions("Save Sessions")*/);
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

}

