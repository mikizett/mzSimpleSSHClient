package com.mz.sshclient.ui.components.session.panels.add_or_edit;


import com.mz.sshclient.model.SessionItemDraftModel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class JumpHostPanel extends JPanel implements IAdjustableSessionItemDraftPanel {

    private final Insets topInset = new Insets(20, 10, 0, 10);
    private final Insets noInset = new Insets(5, 10, 0, 10);
    private final JLabel jumpHostLabel = new JLabel("Jump Host");
    private final JTextField jumpHostTextField = new JTextField(30);
    private final SessionItemDraftModel sessionItemDraftModel;

    public JumpHostPanel(final SessionItemDraftModel sessionItemDraftModel, final AddOrEditEnum addOrEditEnum) {
        this.sessionItemDraftModel = sessionItemDraftModel;
        init();
        if (addOrEditEnum == AddOrEditEnum.EDIT) {
            initData();
        }
    }

    private void init() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = topInset;

        c.gridx = 0;
        c.gridy = 12;
        c.gridwidth = 2;
        c.insets = topInset;
        add(jumpHostLabel, c);

        c.gridx = 0;
        c.gridy = 13;
        c.gridwidth = 1;
        c.insets = noInset;
        c.weightx = 1;
        add(jumpHostTextField, c);

        JPanel panel = new JPanel(new BorderLayout());
        c.gridx = 0;
        c.gridy = 20;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 10;
        c.fill = GridBagConstraints.BOTH;

        add(panel, c);
    }

    private void initData() {
        jumpHostTextField.setText(sessionItemDraftModel.getJumpHost());
    }

    @Override
    public void adjustSessionItemDraft() {
        sessionItemDraftModel.setJumpHost(jumpHostTextField.getText());
    }

}
