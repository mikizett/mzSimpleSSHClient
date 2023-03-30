package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ui.components.common.label.WrappedLabel;
import com.mz.sshclient.utils.AwesomeFontEnum;
import com.mz.sshclient.utils.FileIconUtil;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

public class FolderViewListCellRenderer extends JPanel implements ListCellRenderer<FileInfo> {

    private final JLabel lblIcon;
    private final WrappedLabel lblText;

    public FolderViewListCellRenderer() {
        super(new BorderLayout(10, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        lblIcon = new JLabel();
        lblIcon.setOpaque(true);
        lblIcon.setBorder(new EmptyBorder(0, 20, 0, 20));
        lblIcon.setHorizontalAlignment(JLabel.CENTER);
        lblIcon.setVerticalAlignment(JLabel.CENTER);
        // TODO: set font here: java.awt.Font[family=FontAwesome,name=FontAwesome,style=plain,size=48]
        //lblIcon.setFont(App.skin.getIconFont().deriveFont(Font.PLAIN, 48.f));
        lblIcon.setText(AwesomeFontEnum.FA_FOLDER.getAwesomeFontType());

        this.lblText = new WrappedLabel();

        this.add(this.lblIcon);
        this.add(lblText, BorderLayout.SOUTH);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends FileInfo> list,
            FileInfo value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        this.lblIcon.setText(getIconForType(value));
        this.lblIcon.setBackground(list.getBackground());
        this.lblIcon.setForeground(isSelected ? list.getSelectionBackground() : list.getForeground());
        this.lblText.setBackground(list.getBackground());
        this.lblText.setForeground(isSelected ? list.getSelectionBackground() : list.getForeground());
        this.lblText.setText(value.getName());
        this.setBackground(list.getBackground());
        return this;
    }

    public String getIconForType(FileInfo ent) {
        return FileIconUtil.getIconForType(ent);
    }

}
