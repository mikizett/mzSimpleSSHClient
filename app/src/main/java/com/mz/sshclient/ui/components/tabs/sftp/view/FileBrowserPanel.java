package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ui.components.tabs.sftp.view.table.FileBrowserTable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public class FileBrowserPanel extends JPanel {

    private final FileBrowserTable table;
    private final JScrollPane tableScroller;

    private boolean showHiddenFiles = false;
    private List<FileInfo> files;

    public FileBrowserPanel(FileBrowserEventListener listener) {
        super(new BorderLayout());

        table = new FileBrowserTable(listener);
        table.resizeColumnWidth();

        tableScroller = new JScrollPane(table);
        table.setRowHeight(table.getTableCellLabelRenderer().getHeight());

        table.resizeColumnWidth();

        refreshViewMode();
    }

    public FileBrowserTable getFileBrowserTable() {
        return table;
    }

    public FileInfo[] getFiles() {
        if (files == null) {
            return new FileInfo[0];
        } else {
            FileInfo[] fs = new FileInfo[files.size()];
            for (int i = 0; i < files.size(); i++) {
                fs[i] = files.get(i);
            }
            return fs;
        }
    }

    public void setItems(List<FileInfo> list) {
        this.files = list;
        applyFilter();
    }

    public void setFolderViewTransferHandler(DndTransferHandler transferHandler) {
        table.setTransferHandler(transferHandler);
    }

    public void setShowHiddenFiles(boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
        applyFilter();
    }

    private void applyFilter() {
        table.getFileBrowserTableModel().clear();
        if (!this.showHiddenFiles) {
            List<FileInfo> fileInfoList = new ArrayList<>(0);
            for (FileInfo info : files) {
                if (!info.getName().startsWith(".")) {
                    fileInfoList.add(info);
                }
            }
            table.getFileBrowserTableModel().addAll(fileInfoList);
        } else {
            table.getFileBrowserTableModel().addAll(files);
        }
    }

    public void refreshViewMode() {
        add(tableScroller);

        revalidate();
        repaint(0);
    }

}
