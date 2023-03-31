package com.mz.sshclient.ui.components.tabs.sftp.view.table;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.TableRowSorter;
import java.util.Comparator;
import java.util.List;

public class FileBrowserTableSorter extends TableRowSorter<FileBrowserTableModel> {

    private final SortOrder[] sortingOrder = { null };

    public FileBrowserTableSorter(final FileBrowserTableModel fileBrowserTableModel) {
        super(fileBrowserTableModel);
        init();
    }

    public void sort(int index, SortOrder sortOrder) {
        setSortKeys(List.of(new RowSorter.SortKey(index, sortOrder)));
        sort();
    }

    private void init() {
        addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                final List<? extends SortKey> sortKeys = e.getSource().getSortKeys();
                if (!sortKeys.isEmpty()) {
                    sortingOrder[0] = sortKeys.get(0).getSortOrder();
                }
            }
        });

        initSortByName();
        initSortBySize();
        initSortByType();
        initSortByLastModified();
        initSortByPermission();
        initSortByOwner();
    }

    private void initSortByName() {
        setComparator(0, (Comparator) (o1, o2) -> {
            FileInfo fi1 = (FileInfo) o1;
            FileInfo fi2 = (FileInfo) o2;
            // make sure folders are always before files with respect to current sort order
            if (fi1.isDirectory()) {
                if (!fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? 1 : -1;
                }
            } else {
                if (fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? -1 : 1;
                }
            }

            return fi1.getName().compareToIgnoreCase(fi2.getName());
        });
    }

    private void initSortBySize() {
        setComparator(2, (Comparator) (o1, o2) -> {
            FileInfo fi1 = (FileInfo) o1;
            FileInfo fi2 = (FileInfo) o2;
            //Make sure folders are always before files with respect to current sort order
            if (fi1.isDirectory()) {
                if (!fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? 1 : -1;
                }
            } else {
                if (fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? -1 : 1;
                }
            }

            Long s1 = fi1.getSize();
            Long s2 = fi2.getSize();
            return s1.compareTo(s2);
        });
    }

    private void initSortByType() {
        setComparator(3, (Comparator) (o1, o2) -> {
            String s1 = ((FileInfo) o1).getType().toString();
            String s2 = ((FileInfo) o2).getType().toString();
            return s1.compareTo(s2);
        });
    }

    private void initSortByLastModified() {
        setComparator(1, (Comparator) (o1, o2) -> {
            FileInfo fi1 = (FileInfo) o1;
            FileInfo fi2 = (FileInfo) o2;
            //Make sure folders are always before files with respect to current sort order
            if (fi1.isDirectory()) {
                if (!fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? 1 : -1;
                }
            } else {
                if (fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? -1 : 1;
                }
            }

            return fi1.getLastModified().compareTo(fi2.getLastModified());
        });
    }

    private void initSortByPermission() {
        setComparator(4, (Comparator) (o1, o2) -> {
            FileInfo fi1 = (FileInfo) o1;
            FileInfo fi2 = (FileInfo) o2;
            //Make sure folders are always before files with respect to current sort order
            if (fi1.isDirectory()) {
                if (!fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? 1 : -1;
                }
            } else {
                if (fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? -1 : 1;
                }
            }

            String s1 = fi1.getPermissionString();
            String s2 = fi2.getPermissionString();
            return s1.compareTo(s2);
        });
    }

    private void initSortByOwner() {
        setComparator(5, (Comparator) (o1, o2) -> {
            FileInfo fi1 = (FileInfo) o1;
            FileInfo fi2 = (FileInfo) o2;
            //Make sure folders are always before files with respect to current sort order
            if (fi1.isDirectory()) {
                if (!fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? 1 : -1;
                }
            } else {
                if (fi2.isDirectory()) {
                    return sortingOrder[0] == SortOrder.DESCENDING ? -1 : 1;
                }
            }

            String s1 = fi1.getUser();
            String s2 = fi2.getUser();
            return s1.compareTo(s2);
        });
    }

    public int getSortIndex() {
        for (RowSorter.SortKey sortKey : getSortKeys()) {
            return sortKey.getColumn();
        }
        return -1;
    }

    public boolean isSortAsc() {
        for (RowSorter.SortKey sortKey : getSortKeys()) {
            return sortKey.getSortOrder() == SortOrder.ASCENDING;
        }
        return false;
    }

}
