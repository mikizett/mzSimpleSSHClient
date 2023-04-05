package com.mz.sshclient.ui.components.session.panels.add_or_edit;

import com.mz.sshclient.model.SessionItemDraftModel;
import com.mz.sshclient.ui.components.common.field.OnlyNumbersTextField;
import com.mz.sshclient.ui.events.listener.IValueChangeListener;
import com.mz.sshclient.ui.events.listener.InputFieldDocumentListener;
import com.mz.sshclient.ui.utils.UIUtils;
import com.mz.sshclient.utils.Utils;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class ConnectionPanel extends JPanel implements IAdjustableSessionItemDraftPanel {

    private final SessionItemDraftModel sessionItemDraftModel;

    private final Insets topInset = new Insets(20, 10, 0, 10);
    private final Insets noInset = new Insets(5, 10, 0, 10);

    private final JLabel hostLabel = new JLabel("Host");
    private final JTextField hostTextField = new JTextField(10);

    private final JLabel portLabel = new JLabel("Port");
    private final OnlyNumbersTextField portTextField = new OnlyNumbersTextField("22", 5);

    private final JLabel userLabel = new JLabel("User");
    private final JTextField userTextField = new JTextField(10);

    private final JLabel passLabel = new JLabel("Password");
    private final JPasswordField passField = new JPasswordField(10);

    private final JLabel privateKeyFileLabel = new JLabel("Private key file");
    private final JTextField privateKeyFileTextField = new JTextField(10);

    private final JButton privateKeyBrowseButton = new JButton("Browse");
    private final JButton showPassButton = new JButton("Show");

    private IValueChangeListener changeValueListener;

    public ConnectionPanel(
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

        hostLabel.setHorizontalAlignment(JLabel.LEADING);
        portTextField.setHorizontalAlignment(JTextField.RIGHT);

        privateKeyBrowseButton.addActionListener(e -> {
            final String privateKeyLocation = UIUtils.showFileChooserPrivateKey(this);
            privateKeyFileTextField.setText(privateKeyLocation);
        });

        showPassButton.addActionListener(e -> {
            final JTextArea textArea = new JTextArea();
            textArea.setText(new String(passField.getPassword()));
            textArea.setEditable(false);
            textArea.setLineWrap(false);
            JOptionPane.showMessageDialog(this, textArea, "Password", JOptionPane.PLAIN_MESSAGE);
        });

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.insets = topInset;
        add(hostLabel, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.insets = noInset;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(hostTextField, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.insets = topInset;
        add(portLabel, c);

        c.gridx = 0;
        c.gridy = 4;
        c.ipady = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.insets = noInset;
        add(portTextField, c);

        c.gridx = 0;
        c.gridy = 5;
        c.insets = topInset;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        add(userLabel, c);

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        c.insets = noInset;
        add(userTextField, c);

        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 2;
        c.insets = topInset;
        add(passLabel, c);

        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 1;
        c.insets = noInset;
        c.weightx = 1;
        add(passField, c);

        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 1;
        c.weightx = 0;
        c.insets = new Insets(5, 0, 0, 8);
        add(showPassButton, c);

        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 2;
        c.insets = topInset;
        add(privateKeyFileLabel, c);

        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 1;
        c.insets = noInset;
        c.weightx = 2;
        add(privateKeyFileTextField, c);

        c.gridx = 1;
        c.gridy = 10;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 0, 0, 10);
        add(privateKeyBrowseButton, c);

        JPanel panel = new JPanel(new BorderLayout());
        c.gridx = 0;
        c.gridy = 11;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 10;
        c.fill = GridBagConstraints.BOTH;

        add(panel, c);

        setFocusable(false);
    }

    private void initData() {
        hostTextField.setText(sessionItemDraftModel.getHost());
        portTextField.setText(sessionItemDraftModel.getPort());
        userTextField.setText(sessionItemDraftModel.getUser());
        passField.setText(Utils.decodeString(sessionItemDraftModel.getPassword()));
        privateKeyFileTextField.setText(sessionItemDraftModel.getPrivateKeyFile());
    }

    private void addListeners() {
        hostTextField.getDocument().addDocumentListener(new InputFieldDocumentListener(changeValueListener));
        portTextField.getDocument().addDocumentListener(new InputFieldDocumentListener(changeValueListener));
        userTextField.getDocument().addDocumentListener(new InputFieldDocumentListener(changeValueListener));
        passField.getDocument().addDocumentListener(new InputFieldDocumentListener(changeValueListener));
        privateKeyFileTextField.getDocument().addDocumentListener(new InputFieldDocumentListener(changeValueListener));
    }

    public String getHost() {
        return hostTextField.getText();
    }

    public String getPort() {
        return portTextField.getText();
    }

    public String getUser() {
        return userTextField.getText();
    }

    public boolean hasValueChanged() {
        return !hostTextField.getText().equals(sessionItemDraftModel.getHost()) ||
                !portTextField.getText().equals(sessionItemDraftModel.getPort()) ||
                !userTextField.getText().equals(sessionItemDraftModel.getUser()) ||
                !Utils.encodeString(new String(passField.getPassword())).equals(sessionItemDraftModel.getPassword()) ||
                !privateKeyFileTextField.getText().equals(sessionItemDraftModel.getPrivateKeyFile());
    }

    @Override
    public void adjustSessionItemDraft() {
        sessionItemDraftModel.setHost(hostTextField.getText());
        sessionItemDraftModel.setPort(portTextField.getText());
        sessionItemDraftModel.setUser(userTextField.getText());
        sessionItemDraftModel.setPassword(Utils.encodeString(new String(passField.getPassword())));
        sessionItemDraftModel.setPrivateKeyFile(privateKeyFileTextField.getText());

        // reset passwd
        passField.setText("0");
    }

}
