package com.mz.sshclient.ui.config;

import com.mz.sshclient.model.appsettings.AppSettingsModel;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;

public class GlobalSettingsPanel extends JPanel {

    private JTextField jumpHostTextField = new JTextField();
    private JButton addJumpHostButton = new JButton("Add");
    private JComboBox<String> jumpHostsComboBox = new JComboBox<>();
    private JButton removeSelectedJumpHostButton = new JButton("Remove selected jump host");
    private JCheckBox darkMode = new JCheckBox("Dark mode (needs restart)");

    public GlobalSettingsPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        Box vbox = Box.createVerticalBox();

        jumpHostsComboBox.setEditable(false);

        Dimension d1 = new Dimension(Math.max(200, jumpHostsComboBox.getPreferredSize().width * 2), jumpHostsComboBox.getPreferredSize().height);

        jumpHostTextField.setMaximumSize(d1);
        jumpHostTextField.setMinimumSize(d1);
        jumpHostTextField.setPreferredSize(d1);

        jumpHostsComboBox.setMaximumSize(d1);
        jumpHostsComboBox.setMinimumSize(d1);
        jumpHostsComboBox.setPreferredSize(d1);

        vbox.add(createRow(new JLabel("Add jump host"), Box.createHorizontalGlue(), jumpHostTextField));
        vbox.add(Box.createRigidArea(new Dimension(10, 10)));
        vbox.add(createRow(new JLabel(""), Box.createHorizontalGlue(), addJumpHostButton));

        addJumpHostButton.addActionListener(e -> {
            final String jumpHost = jumpHostTextField.getText();
            if (!jumpHost.isEmpty() && !jumpHost.isBlank()) {
                jumpHostsComboBox.addItem(jumpHost);
                // just select the first added item
                final String selectedItem = (String) jumpHostsComboBox.getSelectedItem();
                if (selectedItem.isEmpty() && selectedItem.isBlank()) {
                    jumpHostsComboBox.setSelectedItem(jumpHost);
                }
                jumpHostTextField.setText("");
            }
        });

        vbox.add(Box.createRigidArea(new Dimension(10, 30)));
        vbox.add(createRow(new JLabel("Select default jump host"), Box.createHorizontalGlue(), jumpHostsComboBox));

        final String[] jumpHostArr = AppSettings.getJumpHosts();
        if (jumpHostArr.length > 0) {
            Arrays.stream(jumpHostArr).forEach(value -> jumpHostsComboBox.addItem(value));

            final String selectedJumpHost = AppSettings.getSelectedJumpHost();
            jumpHostsComboBox.setSelectedItem(selectedJumpHost);
        }

        vbox.add(Box.createRigidArea(new Dimension(10, 10)));
        vbox.add(createRow(new JLabel("Remove selected jump host"), Box.createHorizontalGlue(), removeSelectedJumpHostButton));

        removeSelectedJumpHostButton.addActionListener(e -> {
            final String selectedJumpHost = (String) jumpHostsComboBox.getSelectedItem();
            if (!selectedJumpHost.isEmpty() && !selectedJumpHost.isBlank()) {
                jumpHostsComboBox.removeItem(selectedJumpHost);
            }
        });

        vbox.add(Box.createRigidArea(new Dimension(10, 30)));
        darkMode.setAlignmentX(Box.LEFT_ALIGNMENT);
        vbox.add(darkMode);

        darkMode.setSelected(AppSettings.isDarkMode());

        vbox.setBorder(new EmptyBorder(30, 10, 10, 10));

        add(vbox);
    }

    private Component createRow(Component... components) {
        Box box = Box.createHorizontalBox();
        box.setAlignmentX(Box.LEFT_ALIGNMENT);
        for (Component c : components) {
            box.add(c);
        }
        return box;
    }

    public void saveToFile() {
        final AppSettingsModel model = new AppSettingsModel();

        int itemSize = jumpHostsComboBox.getModel().getSize();

        // the first item is empty
        if (jumpHostsComboBox.getModel().getSize() > 1)  {
            final String[] jumpHosts = new String[itemSize - 1];
            for (int i = 1; i < itemSize; i++) {
                jumpHosts[i-1] = jumpHostsComboBox.getItemAt(i);
            }
            model.setJumpHosts(jumpHosts);
            model.setSelectedJumpHost((String) jumpHostsComboBox.getSelectedItem());
        }
        model.setDarkMode(darkMode.isSelected());

        try {
            AppSettings.saveToFile(model);
        } catch (IOException e) {
            final String msg = "Could not save app settings file!";
            MessageDisplayUtil.showErrorMessage(msg);
        }
    }
}
