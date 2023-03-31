package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ui.components.tabs.sftp.view.table.FileBrowserTableModel;
import com.mz.sshclient.utils.FileIconUtil;
import com.mz.sshclient.utils.FormatUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

public class TableCellLabelRenderer implements TableCellRenderer {

    private final JPanel panel;
    private final JLabel textLabel;
    private final JLabel iconLabel;
    private final JLabel label;
    private final int height;
    private final Color foreground = new Color(80, 80, 80);

    public TableCellLabelRenderer() {
        panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(new EmptyBorder(5, 10, 5, 5));
        textLabel = new JLabel();
        textLabel.setForeground(foreground);
        textLabel.setText("AAA");
        textLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        iconLabel = new JLabel();
        // TODO: set a font here: it should be this one: java.awt.Font[family=FontAwesome,name=FontAwesome,style=plain,size=20]
        //Font f = App.skin.getIconFont().deriveFont(Font.PLAIN, 20.f);
        //iconLabel.setFont(App.skin.getIconFont().deriveFont(Font.PLAIN, 20.f));
        iconLabel.setText("\uf016");
        iconLabel.setForeground(foreground);

        Dimension d1 = iconLabel.getPreferredSize();
        iconLabel.setText("\uf07b");
        Dimension d2 = iconLabel.getPreferredSize();

        height = Math.max(d1.height, d2.height) + 10;

        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(textLabel);
        panel.add(iconLabel, BorderLayout.WEST);

        panel.doLayout();

        label = new JLabel();
        label.setForeground(foreground);
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        label.setOpaque(true);
    }

    public int getHeight() {
        return height;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        FileBrowserTableModel folderViewModel = (FileBrowserTableModel) table.getModel();

        int r = table.convertRowIndexToModel(row);
        int c = table.convertColumnIndexToModel(column);

        FileInfo ent = folderViewModel.getItemAt(r);

        panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        textLabel.setForeground(isSelected ? table.getSelectionForeground() : foreground);
        iconLabel.setForeground(isSelected ? table.getSelectionForeground() : foreground);
        iconLabel.setText(getIconForType(ent));
        textLabel.setText(ent.getName());

        label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        label.setForeground(isSelected ? table.getSelectionForeground() : foreground);

        switch (c) {
            case 0:
                label.setText("");
                break;
            case 1:
                label.setText(FormatUtils.formatDate(ent.getLastModified()));
                break;
            case 2:
                if (ent.getType() == FileType.Directory || ent.getType() == FileType.DirLink) {
                    label.setText("");
                } else {
                    label.setText(FormatUtils.humanReadableByteCount(ent.getSize(), true));
                }
                break;
            case 3:
                label.setText(ent.getType() + "");
                break;
            case 4:
                label.setText(ent.getPermissionString());
                break;
            case 5:
                label.setText(ent.getUser());
                break;
            default:
                break;
        }

        if (c == 0) {
            return panel;
        } else {
            return label;
        }

    }

    public String getIconForType(FileInfo ent) {
        return FileIconUtil.getIconForType(ent);
    }

}
