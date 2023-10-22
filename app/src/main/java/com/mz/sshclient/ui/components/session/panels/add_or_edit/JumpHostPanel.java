package com.mz.sshclient.ui.components.session.panels.add_or_edit;


import com.mz.sshclient.model.session.SessionItemDraftModel;
import com.mz.sshclient.ui.config.AppSettings;
import com.mz.sshclient.ui.events.listener.IValueChangeListener;
import com.mz.sshclient.ui.events.listener.InputFieldDocumentListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Objects;

public class JumpHostPanel extends JPanel implements IAdjustableSessionItemDraftPanel {

    private final Insets topInset = new Insets(20, 10, 0, 10);
    private final Insets noInset = new Insets(5, 10, 0, 10);
    private final JLabel jumpHostLabel = new JLabel("Jump Host");
    private final JComboBox<String> jumpHostComboBox = new JComboBox<>();
    private final SessionItemDraftModel sessionItemDraftModel;
    private final IValueChangeListener changeValueListener;

    public JumpHostPanel(
            final SessionItemDraftModel sessionItemDraftModel,
            final AddOrEditEnum addOrEditEnum,
            final IValueChangeListener changeValueListener
    ) {
        this.sessionItemDraftModel = sessionItemDraftModel;
        this.changeValueListener = changeValueListener;
        init();
        if (addOrEditEnum == AddOrEditEnum.EDIT) {
            initData();
        }
        addListeners();
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

        final String[] jumpHosts = AppSettings.getJumpHosts();
        Arrays.stream(jumpHosts).forEach(jumpHostComboBox::addItem);

        jumpHostComboBox.setEditable(false);

        c.gridx = 0;
        c.gridy = 13;
        c.gridwidth = 1;
        c.insets = noInset;
        c.weightx = 1;
        add(jumpHostComboBox, c);

        JPanel panel = new JPanel(new BorderLayout());
        c.gridx = 0;
        c.gridy = 20;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 10;
        c.fill = GridBagConstraints.BOTH;

        add(panel, c);

        setFocusable(false);
    }

    private void initData() {
        jumpHostComboBox.setSelectedItem(sessionItemDraftModel.getJumpHost());
    }

    private void addListeners() {
        jumpHostComboBox.addItemListener(new InputFieldDocumentListener(changeValueListener));
    }

    public String getJumpHost() {
        return (String) jumpHostComboBox.getSelectedItem();
    }

    public boolean hasValueChanged() {
        return !Objects.equals(jumpHostComboBox.getSelectedItem(), sessionItemDraftModel.getJumpHost());
    }

    @Override
    public void adjustSessionItemDraft() {
        sessionItemDraftModel.setJumpHost((String) jumpHostComboBox.getSelectedItem());
    }

}
