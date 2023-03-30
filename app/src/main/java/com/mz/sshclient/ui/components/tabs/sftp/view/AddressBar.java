package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ui.components.common.field.SkinnedTextField;
import com.mz.sshclient.utils.LayoutUtil;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class AddressBar extends JPanel {

    private final AddressBarBreadCrumbs addressBar;
    private final JComboBox<String> txtAddressBar;
    private final JButton btnEdit;
    private final JPanel addrPanel;
    private boolean updating = false;
    private ActionListener a;
    private JPopupMenu popup;
    private final char separator;
    private final JPanel panBtn2;

    public AddressBar(char separator, ActionListener popupTriggeredListener) {
        setLayout(new BorderLayout());
        addrPanel = new JPanel(new BorderLayout());
        addrPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        this.separator = separator;

        JButton btnRoot = new JButton();
        // TODO: check this
        //UIDefaults toolbarSkin = App.skin.createToolbarSkin();
        //btnRoot.putClientProperty("Nimbus.Overrides", toolbarSkin);
        // TODO: check font here:
        //btnRoot.setFont(App.skin.getIconFont());
        btnRoot.setText("\uf0a0");
        btnRoot.addActionListener(e -> createAndShowPopup());

        DefaultComboBoxModel<String> model1 = new DefaultComboBoxModel<>();
        txtAddressBar = new JComboBox<>(model1);
        txtAddressBar.setEditor(new AddressBarComboBoxEditor());
        txtAddressBar.putClientProperty("paintNoBorder", "True");

        txtAddressBar.addActionListener(e -> {
            if (updating) {
                return;
            }
            String item = (String) txtAddressBar.getSelectedItem();
            if (e.getActionCommand().equals("comboBoxEdited")) {
                ComboBoxModel<String> model = txtAddressBar.getModel();
                boolean found = false;
                for (int i = 0; i < model.getSize(); i++) {
                    if (model.getElementAt(i).equals(item)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    txtAddressBar.addItem(item);
                }
                if (a != null) {
                    a.actionPerformed(new ActionEvent(this, 0, item));
                }
            }
        });
        txtAddressBar.setEditable(true);
        ComboBoxEditor cmdEdit = new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                JTextField textField = new SkinnedTextField(10);
                textField.setBorder(new LineBorder(Color.black, 0));
                textField.setName("ComboBox.textField");
                return textField;
            }
        };
        txtAddressBar.setEditor(cmdEdit);
        addressBar = new AddressBarBreadCrumbs(separator == '/', popupTriggeredListener);
        addressBar.addActionListener(e -> {
            if (a != null) {
                a.actionPerformed(new ActionEvent(this, 0, e.getActionCommand()));
            }
        });

        panBtn2 = new JPanel(new BorderLayout());
        panBtn2.setBorder(new EmptyBorder(3, 3, 3, 3));

        btnEdit = new JButton();
        //btnEdit.putClientProperty("Nimbus.Overrides", toolbarSkin);
        // TODO: check font here
        //btnEdit.setFont(App.skin.getIconFont());
        btnEdit.setText("\uf023");
        btnEdit.addActionListener(e -> {
            if (!isSelected()) {
                switchToText();
            } else {
                switchToPathBar();
            }
            revalidate();
            repaint();
        });
        LayoutUtil.equalizeSize(btnRoot, btnEdit);

        panBtn2.add(btnRoot);

        addrPanel.add(addressBar);
        add(addrPanel);
        JPanel panBtn = new JPanel(new BorderLayout());
        panBtn.setBorder(new EmptyBorder(3, 3, 3, 3));
        panBtn.add(btnEdit);
        add(panBtn, BorderLayout.EAST);
        add(panBtn2, BorderLayout.WEST);
        btnEdit.putClientProperty("toggle.selected", Boolean.FALSE);
    }

    public void switchToPathBar() {
        add(panBtn2, BorderLayout.WEST);
        addrPanel.remove(txtAddressBar);
        addrPanel.add(addressBar);
        btnEdit.setIcon(UIManager.getIcon("AddressBar.edit"));
        btnEdit.putClientProperty("toggle.selected", Boolean.FALSE);
        btnEdit.setText("\uf023");
    }

    public void switchToText() {
        addrPanel.remove(addressBar);
        addrPanel.add(txtAddressBar);
        remove(panBtn2);
        btnEdit.setIcon(UIManager.getIcon("AddressBar.toggle"));
        btnEdit.putClientProperty("toggle.selected", Boolean.TRUE);
        txtAddressBar.getEditor().selectAll();
        btnEdit.setText("\uf13e");
    }

    public String getText() {
        return isSelected() ? (String) txtAddressBar.getSelectedItem() : addressBar.getSelectedText();
    }

    public void setText(String text) {
        updating = true;
        txtAddressBar.setSelectedItem(text);
        addressBar.setPath(text);
        updating = false;
    }

    public void addActionListener(ActionListener e) {
        this.a = e;
    }

    private boolean isSelected() {
        return btnEdit.getClientProperty("toggle.selected") == Boolean.TRUE;
    }

    private void createAndShowPopup() {
        if (popup == null) {
            popup = new JPopupMenu();
        } else {
            popup.removeAll();
        }

        String itemPath = "item.path";
        if (separator == '/') {
            JMenuItem item = new JMenuItem("ROOT");
            item.putClientProperty(itemPath, "/");
            item.addActionListener(e -> {
                String selectedText = (String) item.getClientProperty(itemPath);
                if (a != null) {
                    a.actionPerformed(new ActionEvent(this, 0, selectedText));
                }
            });
            popup.add(item);
        } else {
            File[] roots = File.listRoots();
            for (File f : roots) {
                JMenuItem item = new JMenuItem(f.getAbsolutePath());
                item.putClientProperty(itemPath, f.getAbsolutePath());
                item.addActionListener(e -> {
                    String selectedText = (String) item.getClientProperty(itemPath);
                    if (a != null) {
                        a.actionPerformed(new ActionEvent(this, 0, selectedText));
                    }
                });
                popup.add(item);
            }
        }

        popup.show(this, 0, getHeight());
    }

}
