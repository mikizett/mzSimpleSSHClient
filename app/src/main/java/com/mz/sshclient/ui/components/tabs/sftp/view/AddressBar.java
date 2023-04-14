package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ui.components.common.field.SkinnedTextField;
import com.mz.sshclient.ui.components.tabs.sftp.ssh.SshFileBrowserView;
import com.mz.sshclient.utils.LayoutUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AddressBar extends JPanel {

    private static final Logger LOG = LogManager.getLogger(AddressBar.class);

    private final AddressBarBreadCrumbs addressBar;
    private final JComboBox<String> comboBoxAddressBar;
    private final JButton buttonEdit;
    private final JPanel addressPanel;
    private boolean updating = false;
    private ActionListener actionListenerRef;
    private JPopupMenu popup;
    private final JPanel panBtn2;

    private final SshFileBrowserView sshFileBrowserView;

    public AddressBar(ActionListener popupTriggeredListener) {
        this(null, popupTriggeredListener);
    }

    public AddressBar(SshFileBrowserView sshFileBrowserView, ActionListener popupTriggeredListener) {
        this.sshFileBrowserView = sshFileBrowserView;

        setLayout(new BorderLayout());
        addressPanel = new JPanel(new BorderLayout());
        addressPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

        JButton buttonRoot = new JButton();
        buttonRoot.setText("/");
        buttonRoot.addActionListener(e -> createAndShowPopup());

        DefaultComboBoxModel<String> model1 = new DefaultComboBoxModel<>();
        comboBoxAddressBar = new JComboBox<>(model1);
        comboBoxAddressBar.setEditor(new AddressBarComboBoxEditor());
        comboBoxAddressBar.putClientProperty("paintNoBorder", "True");

        comboBoxAddressBar.addActionListener(e -> {
            if (updating) {
                return;
            }
            String item = (String) comboBoxAddressBar.getSelectedItem();
            if (e.getActionCommand().equals("comboBoxEdited")) {
                ComboBoxModel<String> model = comboBoxAddressBar.getModel();
                boolean found = false;
                for (int i = 0; i < model.getSize(); i++) {
                    if (model.getElementAt(i).equals(item)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    comboBoxAddressBar.addItem(item);
                }
                if (actionListenerRef != null) {
                    actionListenerRef.actionPerformed(new ActionEvent(this, 0, item));
                }
            }
        });

        comboBoxAddressBar.setEditable(true);
        ComboBoxEditor cmdEdit = new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                JTextField textField = new SkinnedTextField(10);
                textField.setBorder(new LineBorder(Color.black, 0));
                textField.setName("ComboBox.textField");
                return textField;
            }
        };
        comboBoxAddressBar.setEditor(cmdEdit);

        addressBar = new AddressBarBreadCrumbs(File.separatorChar == '/', popupTriggeredListener);
        addressBar.addActionListener(e -> {
            if (actionListenerRef != null) {
                actionListenerRef.actionPerformed(new ActionEvent(this, 0, e.getActionCommand()));
            }
        });

        panBtn2 = new JPanel(new BorderLayout());
        panBtn2.setBorder(new EmptyBorder(3, 3, 3, 3));

        buttonEdit = new JButton();
        buttonEdit.setFont(UIManager.getFont("iconFont"));
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

        addressPanel.add(addressBar);
        add(addressPanel);
        JPanel panBtn = new JPanel(new BorderLayout());
        panBtn.setBorder(new EmptyBorder(3, 3, 3, 3));
        panBtn.add(buttonEdit);
        add(panBtn, BorderLayout.EAST);
        add(panBtn2, BorderLayout.WEST);
        buttonEdit.putClientProperty("toggle.selected", Boolean.FALSE);
    }

    public void switchToPathBar() {
        add(panBtn2, BorderLayout.WEST);
        addressPanel.remove(comboBoxAddressBar);
        addressPanel.add(addressBar);
        buttonEdit.setIcon(UIManager.getIcon("AddressBar.edit"));
        buttonEdit.putClientProperty("toggle.selected", Boolean.FALSE);
        buttonEdit.setText("\uf023");
    }

    public void switchToText() {
        addressPanel.remove(addressBar);
        addressPanel.add(comboBoxAddressBar);
        remove(panBtn2);
        buttonEdit.setIcon(UIManager.getIcon("AddressBar.toggle"));
        buttonEdit.putClientProperty("toggle.selected", Boolean.TRUE);
        comboBoxAddressBar.getEditor().selectAll();
        buttonEdit.setText("\uf13e");
    }

    public String getText() {
        return isSelected() ? (String) comboBoxAddressBar.getSelectedItem() : addressBar.getSelectedText();
    }

    public void setText(String text) {
        updating = true;
        comboBoxAddressBar.setSelectedItem(text);
        addressBar.setPath(text);
        updating = false;
    }

    public void addActionListener(ActionListener e) {
        this.actionListenerRef = e;
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

        if (sshFileBrowserView == null) {
            addLocalFileRoots();
        } else {
            addRemoteFileRoots();
        }

        popup.show(this, 0, getHeight());
    }

    private void addLocalFileRoots() {
        final File[] roots = File.listRoots();
        for (File root : roots) {
            popup.add(createMenuItem(root.getAbsolutePath()));

            final File[] subFolders = root.listFiles();
            Arrays.sort(subFolders);
            for (File subFolder : subFolders) {
                popup.add(createMenuItem(subFolder.getAbsolutePath()));
            }
        }
    }

    private void addRemoteFileRoots() {
        try {
            final List<FileInfo> list = sshFileBrowserView.getRemoteFileRoots("/");
            Collections.sort(list, (Comparator.comparing(FileInfo::getName)));
            list.forEach(fileInfo -> popup.add(createMenuItem(fileInfo.getPath())));
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private JMenuItem createMenuItem(String path) {
        final String itemPath = "item.path";
        final JMenuItem item = new JMenuItem(path);
        item.putClientProperty(itemPath, path);
        item.addActionListener(e -> {
            String selectedText = (String) item.getClientProperty(itemPath);
            if (actionListenerRef != null) {
                actionListenerRef.actionPerformed(new ActionEvent(this, 0, selectedText));
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
