package com.mz.sshclient.ui.components.session.panels.add_or_edit;

import com.mz.sshclient.model.SessionItemDraftModel;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class SecureFtpPanel extends JPanel implements IAdjustableSessionItemDraftPanel {

    private final Insets topInset = new Insets(20, 10, 0, 10);
    private final Insets noInset = new Insets(5, 10, 0, 10);

    private final JLabel lblLocalFolder = new JLabel("Local Folder");
    private final JTextField localFolderTextField = new JTextField(10);

    private final JLabel lblRemoteFolder = new JLabel("Remote Folder");
    private final JTextField remoteFolderTextField = new JTextField(10);

    private final SessionItemDraftModel sessionItemDraftModel;

    public SecureFtpPanel(final SessionItemDraftModel sessionItemDraftModel, final AddOrEditEnum addOrEditEnum) {
        this.sessionItemDraftModel = sessionItemDraftModel;
        init();
        if (addOrEditEnum == AddOrEditEnum.EDIT) {
            initData();
        }
    }

    private void init() {
        setLayout(new GridBagLayout());

        final JButton localBrowseButton = new JButton("Browse");
        localBrowseButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileHidingEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                localFolderTextField.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        });

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
        add(lblLocalFolder, c);

        c.gridx = 0;
        c.gridy = 13;
        c.gridwidth = 1;
        c.insets = noInset;
        c.weightx = 1;
        add(localFolderTextField, c);

        c.gridx = 1;
        c.gridy = 13;
        c.gridwidth = 1;
        c.insets = new Insets(5, 0, 0, 10);
        c.weightx = 0;
        add(localBrowseButton, c);

        c.gridx = 0;
        c.gridy = 15;
        c.gridwidth = 2;
        c.insets = topInset;
        add(lblRemoteFolder, c);

        c.gridx = 0;
        c.gridy = 16;
        c.gridwidth = 2;
        c.insets = noInset;
        c.weightx = 1;
        add(remoteFolderTextField, c);

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
        localFolderTextField.setText(sessionItemDraftModel.getLocalFolder());
        remoteFolderTextField.setText(sessionItemDraftModel.getRemoteFolder());
    }

    @Override
    public void adjustSessionItemDraft() {
        sessionItemDraftModel.setLocalFolder(localFolderTextField.getText());
        sessionItemDraftModel.setRemoteFolder(remoteFolderTextField.getText());
    }

}
