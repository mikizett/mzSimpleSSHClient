package com.mz.sshclient.ui.actions;

import com.mz.sshclient.ui.components.session.NewSessionDialog;
import com.mz.sshclient.ui.utils.AWTInvokerUtils;

import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;

public class ActionOpenNewSessionDialog extends AbstractAction {
    private final JTree tree;

    public ActionOpenNewSessionDialog(final JTree tree) {
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final NewSessionDialog dialog = new NewSessionDialog(SwingUtilities.getWindowAncestor(tree), tree);
        AWTInvokerUtils.invokeLaterShowWindow(dialog);
    }
}
