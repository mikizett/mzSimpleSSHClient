package com.mz.sshclient.ui.components.tabs.sftp.view.table;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ui.components.tabs.sftp.view.FileBrowserEventListener;
import com.mz.sshclient.ui.components.tabs.sftp.view.FolderViewKeyHandler;
import com.mz.sshclient.ui.components.tabs.sftp.view.TableCellLabelRenderer;

import javax.swing.AbstractAction;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;

public class FileBrowserTable extends JTable {

    private final FileBrowserTableModel fileBrowserTableModel = new FileBrowserTableModel();
    private final JPopupMenu popup = new JPopupMenu();
    private final TableCellLabelRenderer tableCellLabelRenderer = new TableCellLabelRenderer();
    private final FileBrowserTableSorter fileBrowserTableSorter;

    private final FileBrowserEventListener fileBrowserEventListener;

    public FileBrowserTable(final FileBrowserEventListener fileBrowserEventListener) {
        this.fileBrowserEventListener = fileBrowserEventListener;

        fileBrowserTableSorter = new FileBrowserTableSorter(fileBrowserTableModel);

        init();

        addInputAndActionMap();
        initTableListeners();

        fileBrowserTableSorter.sort(1, SortOrder.DESCENDING);
    }

    private void init() {
        setModel(fileBrowserTableModel);

        setDefaultRenderer(FileInfo.class, tableCellLabelRenderer);
        setDefaultRenderer(Long.class, tableCellLabelRenderer);
        setDefaultRenderer(LocalDateTime.class, tableCellLabelRenderer);
        setDefaultRenderer(Object.class, tableCellLabelRenderer);

        setFillsViewportHeight(true);
        setShowGrid(false);

        setIntercellSpacing(new Dimension(0, 0));
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setRowSorter(fileBrowserTableSorter);
    }

    private void addInputAndActionMap() {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");

        getActionMap().put("Enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                FileInfo[] files = getSelectedFiles();
                if (files.length > 0) {
                    if (files[0].getType() == FileType.Directory || files[0].getType() == FileType.DirLink) {
                        String str = files[0].getPath();
                        fileBrowserEventListener.render(str, true);
                    }
                }
            }
        });
    }

    private void initTableListeners() {
        addKeyListener(new FolderViewKeyHandler(this, fileBrowserTableModel));

        addMouseListener();
    }

    private void addMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
                if (getSelectionModel().getValueIsAdjusting()) {
                    selectRow(e);
                    return;
                }
                if (e.getClickCount() == 2) {
                    Point p = e.getPoint();
                    int r = rowAtPoint(p);
                    int x = getSelectedRow();
                    if (x == -1) {
                        return;
                    }
                    if (r == getSelectedRow()) {
                        FileInfo fileInfo = fileBrowserTableModel.getItemAt(getRow(r));
                        if (fileInfo.getType() == FileType.Directory || fileInfo.getType() == FileType.DirLink) {
                            fileBrowserEventListener.addBack(fileInfo.getPath());
                            fileBrowserEventListener.render(fileInfo.getPath(), true);
                        }
                    }
                } else if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    selectRow(e);
                    fileBrowserEventListener.createMenu(popup, getSelectedFiles());
                    popup.pack();
                    popup.show(FileBrowserTable.this, e.getX(), e.getY());
                }
            }
        });
    }

    private void selectRow(MouseEvent e) {
        int r = rowAtPoint(e.getPoint());
        if (r == -1) {
            clearSelection();
        } else {
            if (getSelectedRowCount() > 0) {
                int[] rows = getSelectedRows();
                for (int row : rows) {
                    if (r == row) {
                        return;
                    }
                }
            }
            setRowSelectionInterval(r, r);
        }
    }

    private int getRow(int r) {
        if (r == -1) {
            return -1;
        }
        return convertRowIndexToModel(r);
    }

    public FileInfo[] getSelectedFiles() {
        int[] indexes = getSelectedRows();
        FileInfo[] fileInfoArr = new FileInfo[indexes.length];
        int i = 0;
        for (int index : indexes) {
            FileInfo info = fileBrowserTableModel.getItemAt(convertRowIndexToModel(index));
            fileInfoArr[i++] = info;
        }
        return fileInfoArr;
    }

    public FileBrowserTableModel getFileBrowserTableModel() {
        return fileBrowserTableModel;
    }

    public TableCellLabelRenderer getTableCellLabelRenderer() {
        return tableCellLabelRenderer;
    }

    public FileBrowserTableSorter getTableSorter() {
        return fileBrowserTableSorter;
    }

    public final void resizeColumnWidth() {
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        final TableColumnModel columnModel = getColumnModel();
        for (int column = 0; column < getColumnCount(); column++) {
            TableColumn col = columnModel.getColumn(column);
            if (column == 0) {
                col.setPreferredWidth(200);
            } else if (column == 3) {
                col.setPreferredWidth(120);
            } else {
                col.setPreferredWidth(100);
            }
        }
    }

}
