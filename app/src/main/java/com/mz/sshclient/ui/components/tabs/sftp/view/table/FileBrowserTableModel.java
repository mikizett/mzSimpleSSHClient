package com.mz.sshclient.ui.components.tabs.sftp.view.table;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class FileBrowserTableModel extends AbstractTableModel implements ListModel<FileInfo> {

    private final List<FileInfo> files = new ArrayList<>();
    private final String[] columns = {
            "Name",
            "Modified",
            "Size",
            "Type",
            "Permission",
            "Owner"
    };
    protected EventListenerList listenerList = new EventListenerList();

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return FileInfo.class;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return files.size();
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return files.get(rowIndex);
    }

    @Override
    public int getSize() {
        return files.size();
    }

    @Override
    public FileInfo getElementAt(int index) {
        return files.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    public void clear() {
        int rows = files.size();
        files.clear();
        fireTableDataChanged();
        fireContentsChanged(this, 0, rows - 1);
    }

    public void addAll(List<FileInfo> list) {
        if (list.size() > 0) {
            int sz = files.size();
            files.addAll(list);
            fireTableDataChanged();
            fireContentsChanged(this, 0, sz - 1);
        }
    }

    public FileInfo getItemAt(int index) {
        return files.get(index);
    }

    public void add(FileInfo ent) {
        int sz = files.size();
        files.add(ent);
        fireTableRowsInserted(sz, sz);
        fireContentsChanged(this, 0, sz - 1);
    }

    protected void fireContentsChanged(Object source, int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }
    }

}
