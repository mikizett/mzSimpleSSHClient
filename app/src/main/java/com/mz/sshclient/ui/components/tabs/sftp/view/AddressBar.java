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
    private final JButton buttonEdit;
    private final JPanel addrPanel;
    private boolean updating = false;
    private ActionListener actionListener;
    private JPopupMenu popup;
    private final char separator;
    private final JPanel panBtn2;

    public AddressBar(char separator, ActionListener popupTriggeredListener) {
        setLayout(new BorderLayout());
        addrPanel = new JPanel(new BorderLayout());
        addrPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        this.separator = separator;

        JButton buttonRoot = new JButton();
        buttonRoot.setText("/");
        buttonRoot.addActionListener(e -> createAndShowPopup());

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
                if (actionListener != null) {
                    actionListener.actionPerformed(new ActionEvent(this, 0, item));
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
            if (actionListener != null) {
                actionListener.actionPerformed(new ActionEvent(this, 0, e.getActionCommand()));
            }
        });

        panBtn2 = new JPanel(new BorderLayout());
        panBtn2.setBorder(new EmptyBorder(3, 3, 3, 3));

        buttonEdit = new JButton();
        buttonEdit.setText("\uf023");
        buttonEdit.addActionListener(e -> {
            if (!isSelected()) {
                switchToText();
            } else {
                switchToPathBar();
            }
            revalidate();
            repaint();
        });
        LayoutUtil.equalizeSize(buttonRoot, buttonEdit);

        panBtn2.add(buttonRoot);

        addrPanel.add(addressBar);
        add(addrPanel);
        JPanel panBtn = new JPanel(new BorderLayout());
        panBtn.setBorder(new EmptyBorder(3, 3, 3, 3));
        panBtn.add(buttonEdit);
        add(panBtn, BorderLayout.EAST);
        add(panBtn2, BorderLayout.WEST);
        buttonEdit.putClientProperty("toggle.selected", Boolean.FALSE);
    }

    public void switchToPathBar() {
        add(panBtn2, BorderLayout.WEST);
        addrPanel.remove(txtAddressBar);
        addrPanel.add(addressBar);
        buttonEdit.setIcon(UIManager.getIcon("AddressBar.edit"));
        buttonEdit.putClientProperty("toggle.selected", Boolean.FALSE);
        buttonEdit.setText("\uf023");
    }

    public void switchToText() {
        addrPanel.remove(addressBar);
        addrPanel.add(txtAddressBar);
        remove(panBtn2);
        buttonEdit.setIcon(UIManager.getIcon("AddressBar.toggle"));
        buttonEdit.putClientProperty("toggle.selected", Boolean.TRUE);
        txtAddressBar.getEditor().selectAll();
        buttonEdit.setText("\uf13e");
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
        this.actionListener = e;
    }

    private boolean isSelected() {
        return buttonEdit.getClientProperty("toggle.selected") == Boolean.TRUE;
    }

    private void createAndShowPopup() {
        if (popup == null) {
            popup = new JPopupMenu();
        } else {
            popup.removeAll();
        }

        String itemPath = "item.path";
        if (separator == '/') {
            addFileRoots();

            /*JMenuItem item = new JMenuItem("ROOT");
            item.putClientProperty(itemPath, "/");
            item.addActionListener(e -> {
                String selectedText = (String) item.getClientProperty(itemPath);
                if (a != null) {
                    a.actionPerformed(new ActionEvent(this, 0, selectedText));
                }
            });
            popup.add(item);*/
        } else {
            File[] roots = File.listRoots();
            for (File f : roots) {
                JMenuItem item = new JMenuItem(f.getAbsolutePath());
                item.putClientProperty(itemPath, f.getAbsolutePath());
                item.addActionListener(e -> {
                    String selectedText = (String) item.getClientProperty(itemPath);
                    if (actionListener != null) {
                        actionListener.actionPerformed(new ActionEvent(this, 0, selectedText));
                    }
                });
                popup.add(item);
            }
        }

        popup.show(this, 0, getHeight());
    }

    private void addFileRoots() {
        final File[] roots = File.listRoots();
        for (File root : roots) {
            popup.add(createMenuItem(root));

            final File[] subFolders = root.listFiles();
            for (File subFolder : subFolders) {
                popup.add(createMenuItem(subFolder));
            }
        }
    }

    private JMenuItem createMenuItem(File f) {
        final String itemPath = "item.path";
        final String path = f.getAbsolutePath();
        final JMenuItem item = new JMenuItem(path);
        item.putClientProperty(itemPath, path);
        item.addActionListener(e -> {
            String selectedText = (String) item.getClientProperty(itemPath);
            if (actionListener != null) {
                actionListener.actionPerformed(new ActionEvent(this, 0, selectedText));
            }
        });
        return item;
    }

    public static void main(String[] args) {
        File[] roots = File.listRoots();
        for (File f : roots) {
            System.out.println("BLA -> " + f.getAbsolutePath());
            File[] subFolders = f.listFiles();
            for (File f1 : subFolders) {
                System.out.println("SUB -> " + f1.getAbsolutePath());
            }
        }
    }

}
