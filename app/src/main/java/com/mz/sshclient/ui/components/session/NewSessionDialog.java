package com.mz.sshclient.ui.components.session;

import com.mz.sshclient.ui.components.session.panels.add_or_edit.AddOrEditSessionPanel;
import com.mz.sshclient.ui.utils.AWTInvokerUtils;

import javax.swing.JDialog;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class NewSessionDialog extends JDialog {

    private final Window parentWindow;
    private final JTree tree;

    public NewSessionDialog(final Window parentWindow, final JTree tree) {
        super(parentWindow, "Create new session...");
        this.parentWindow = parentWindow;
        this.tree = tree;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        setResizable(false);
        setSize(500, 540);
        setModal(true);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        add(new AddOrEditSessionPanel(this, tree));

        setLocationRelativeTo(parentWindow);

        AWTInvokerUtils.invokeLaterShowWindow(this);
    }
}
