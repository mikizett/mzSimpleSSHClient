package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ui.components.tabs.sftp.AbstractFileBrowserView;
import com.mz.sshclient.ui.components.tabs.sftp.FileBrowser;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SortOrder;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class OverflowMenuHandler {

    private final JRadioButtonMenuItem mSortName;
    private final JRadioButtonMenuItem mSortSize;
    private final JRadioButtonMenuItem mSortModified;
    private final JRadioButtonMenuItem mSortAsc;
    private final JRadioButtonMenuItem mSortDesc;
    private final JCheckBoxMenuItem mShowHiddenFiles;
    private final KeyStroke ksHideShow;
    private final AbstractAction aHideShow;
    private final JPopupMenu popup;
    private final AbstractFileBrowserView fileBrowserView;
    private final JMenu mSortMenu;
    private final FileBrowser fileBrowser;
    private FileBrowserPanel fileBrowserPanel;

    public OverflowMenuHandler(AbstractFileBrowserView fileBrowserView, FileBrowser fileBrowser) {
        this.fileBrowserView = fileBrowserView;
        this.fileBrowser = fileBrowser;
        ksHideShow = KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK);

        mShowHiddenFiles = new JCheckBoxMenuItem("Show hidden files");
        // TODO: add a setting to enable/disable showing hidden files
        mShowHiddenFiles.setSelected(false);

        aHideShow = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mShowHiddenFiles.setSelected(!mShowHiddenFiles.isSelected());
                hideOptAction();
            }
        };

        mShowHiddenFiles.addActionListener(e -> {
            hideOptAction();
        });
        mShowHiddenFiles.setAccelerator(ksHideShow);

        ButtonGroup bg1 = new ButtonGroup();

        mSortName = createSortMenuItem("Name", 0, bg1);

        mSortSize = createSortMenuItem("Size", 2, bg1);

        mSortModified = createSortMenuItem("Modification date", 3, bg1);

        ButtonGroup bg2 = new ButtonGroup();

        mSortAsc = createSortMenuItem("Sort ascending", 0, bg2);

        mSortDesc = createSortMenuItem("Sort descending", 1, bg2);

        popup = new JPopupMenu();
        mSortMenu = new JMenu("Sort");

        mSortMenu.add(mSortName);
        mSortMenu.add(mSortSize);
        mSortMenu.add(mSortModified);
        mSortMenu.addSeparator();
        mSortMenu.add(mSortAsc);
        mSortMenu.add(mSortDesc);

        popup.add(mShowHiddenFiles);
    }

    private void hideOptAction() {
        fileBrowserPanel.setShowHiddenFiles(mShowHiddenFiles.isSelected());
    }

    private JRadioButtonMenuItem createSortMenuItem(String text, Integer index, ButtonGroup bg) {
        JRadioButtonMenuItem mSortItem = new JRadioButtonMenuItem(text);
        mSortItem.putClientProperty("sort.index", index);
        mSortItem.addActionListener(e -> {
            sortMenuClicked(mSortItem);
        });
        bg.add(mSortItem);
        return mSortItem;
    }

    private void sortMenuClicked(JRadioButtonMenuItem mSortItem) {
        if (mSortItem == mSortAsc) {
            fileBrowserPanel.getFileBrowserTable().getTableSorter().sort(
                    fileBrowserPanel.getFileBrowserTable().getTableSorter().getSortIndex(),
                    SortOrder.ASCENDING
            );
        } else if (mSortItem == mSortDesc) {
            fileBrowserPanel.getFileBrowserTable().getTableSorter().sort(
                    fileBrowserPanel.getFileBrowserTable().getTableSorter().getSortIndex(),
                    SortOrder.DESCENDING
            );
        } else {
            int index = (int) mSortItem.getClientProperty("sort.index");
            fileBrowserPanel.getFileBrowserTable().getTableSorter().sort(
                    index,
                    fileBrowserPanel.getFileBrowserTable().getTableSorter().isSortAsc() ? SortOrder.ASCENDING : SortOrder.DESCENDING
            );
        }
    }

    public JPopupMenu getOverflowMenu() {
        return popup;
    }

    public void setFolderView(FileBrowserPanel fileBrowserPanel) {
        InputMap map = fileBrowserPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap act = fileBrowserPanel.getActionMap();
        this.fileBrowserPanel = fileBrowserPanel;
        map.put(ksHideShow, "ksHideShow");
        act.put("ksHideShow", aHideShow);
    }

}
