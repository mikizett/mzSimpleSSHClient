package com.mz.sshclient.ui.components.tabs.sftp.local;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ssh.sftp.filesystem.local.LocalFileSystem;
import com.mz.sshclient.ui.components.tabs.sftp.FileBrowser;
import com.mz.sshclient.ui.components.tabs.sftp.view.FolderView;
import com.mz.sshclient.utils.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalMenuHandler {

    private static final Logger LOG = LogManager.getLogger(LocalMenuHandler.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final FileBrowser fileBrowser;
    private final LocalFileOperations fileOperations;
    private final LocalFileBrowserView fileBrowserView;
    private JMenuItem mRename, mDelete, mNewFile, mNewFolder, mCopy, mPaste, mCut, mOpen;
    private FolderView folderView;

    public LocalMenuHandler(FileBrowser fileBrowser, LocalFileBrowserView fileBrowserView) {
        this.fileBrowser = fileBrowser;
        this.fileOperations = new LocalFileOperations();
        this.fileBrowserView = fileBrowserView;
    }

    public void initMenuHandler(FolderView folderView) {
        this.folderView = folderView;
        InputMap map = folderView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap act = folderView.getActionMap();
        this.initMenuItems();
    }

    private void initMenuItems() {
        mOpen = new JMenuItem("Open");
        mOpen.addActionListener(e -> open());

        mRename = new JMenuItem("Rename");
        mRename.addActionListener(e -> rename(folderView.getSelectedFiles()[0], fileBrowserView.getCurrentDirectory()));

        mDelete = new JMenuItem("Delete");
        mDelete.addActionListener(e -> {
            delete(folderView.getSelectedFiles());
            fileBrowserView.reload();
        });

        mNewFile = new JMenuItem("New File");
        mNewFile.addActionListener(e -> newFile());

        mNewFolder = new JMenuItem("New Folder");
        mNewFolder.addActionListener(e -> newFolder(fileBrowserView.getCurrentDirectory()));

        mCopy = new JMenuItem("Copy");
        mCopy.addActionListener(e -> {
        });

        mPaste = new JMenuItem("Paste");
        mPaste.addActionListener(e -> {
        });

        mCut = new JMenuItem("Cut");
        mCut.addActionListener(e -> {
        });
    }

    public void createMenu(JPopupMenu popup, FileInfo[] selectedFiles) {
        createMenuContext(popup, selectedFiles);
    }

    private void createMenuContext(JPopupMenu popup, FileInfo[] files) {
        popup.removeAll();
        int selectionCount = files.length;
        createBuildInItems1(selectionCount, popup, files);
        createBuildInItems2(popup);
    }

    private void createBuildInItems1(int selectionCount, JPopupMenu popup, FileInfo[] selectedFiles) {
        if (selectionCount == 1) {
            if (selectedFiles[0].getType() == FileType.File || selectedFiles[0].getType() == FileType.FileLink) {
                popup.add(mOpen);
            }
            popup.add(mRename);
            popup.add(mDelete);
        }
    }

    private void createBuildInItems2(JPopupMenu popup) {
        popup.add(mNewFolder);
        popup.add(mNewFile);
    }

    private void open() {
        FileInfo[] files = folderView.getSelectedFiles();
        if (files.length == 1) {
            FileInfo file = files[0];
            if (file.getType() == FileType.FileLink || file.getType() == FileType.File) {
            }
        }
    }

    private void rename(FileInfo info, String baseFolder) {
        String text = JOptionPane.showInputDialog("Please enter new name", info.getName());
        if (text != null && text.length() > 0) {
            renameAsync(info.getPath(), PathUtils.combineUnix(PathUtils.getParent(info.getPath()), text), baseFolder);
        }
    }

    private void renameAsync(String oldName, String newName, String baseFolder) {
        executor.submit(() -> {
            if (fileOperations.rename(oldName, newName)) {
                fileBrowserView.render(baseFolder);
            }
        });
    }

    private void delete(FileInfo[] selectedFiles) {
        executor.submit(() -> {
            for (FileInfo f : selectedFiles) {
                try {
                    new LocalFileSystem().delete(f);
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        });
    }

    private void newFile() {
        executor.submit(() -> {
            String baseFolder = fileBrowserView.getCurrentDirectory();
            if (fileOperations.newFile(baseFolder)) {
                fileBrowserView.render(baseFolder);
            }
        });
    }

    private void newFolder(String currentDirectory) {
        executor.submit(() -> {
            String baseFolder = currentDirectory;
            if (fileOperations.newFolder(baseFolder)) {
                fileBrowserView.render(baseFolder);
            }
        });
    }

    public JPopupMenu createAddressPopup() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mCopyPath = new JMenuItem("Copy Path");
        popupMenu.add(mCopyPath);

        mCopyPath.addActionListener(e -> {
            String path = popupMenu.getName();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(path), null);
        });
        return popupMenu;
    }

}
