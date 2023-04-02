package com.mz.sshclient.ui.components.tabs.sftp.ssh;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ssh.sftp.filesystem.local.LocalFileSystem;
import com.mz.sshclient.ui.components.common.animation.ConnectAnimationComponent;
import com.mz.sshclient.ui.components.tabs.sftp.FileBrowser;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferData;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferHandler;
import com.mz.sshclient.ui.components.tabs.sftp.view.FileBrowserPanel;
import com.mz.sshclient.utils.PathUtils;
import com.mz.sshclient.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SshMenuHandler {

    private static final Logger LOG = LogManager.getLogger(SshMenuHandler.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ConnectAnimationComponent animationComponent = new ConnectAnimationComponent(mzSimpleSshClientMain.MAIN_FRAME, "Progress ");

    private JMenuItem mCut, mCopy, mCopyPath, mDownload, mPaste, mRename, mDelete, mNewFolder, mNewFile, mUpload;


    private final FileBrowser fileBrowser;
    private final SshFileOperations fileOperations;
    private final SshFileBrowserView fileBrowserView;
    private FileBrowserPanel fileBrowserPanel;

    public SshMenuHandler(FileBrowser fileBrowser, SshFileBrowserView fileBrowserView) {
        this.fileBrowser = fileBrowser;
        this.fileOperations = new SshFileOperations();
        this.fileBrowserView = fileBrowserView;
    }

    public void initMenuHandler(FileBrowserPanel fileBrowserPanel) {
        this.fileBrowserPanel = fileBrowserPanel;
        InputMap map = fileBrowserPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap act = fileBrowserPanel.getActionMap();
        this.initMenuItems(map, act);
    }

    private void initMenuItems(InputMap map, ActionMap act) {
        AbstractAction aRename = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rename(fileBrowserPanel.getFileBrowserTable().getSelectedFiles()[0], fileBrowserView.getCurrentDirectory());
            }
        };
        KeyStroke ksRename = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        mRename = new JMenuItem("Rename");
        mRename.addActionListener(aRename);
        map.put(ksRename, "mRename");
        act.put("mRename", aRename);
        mRename.setAccelerator(ksRename);

        KeyStroke ksDelete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        AbstractAction aDelete = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete(fileBrowserPanel.getFileBrowserTable().getSelectedFiles(), fileBrowserView.getCurrentDirectory());
            }
        };
        mDelete = new JMenuItem("delete");
        mDelete.addActionListener(aDelete);
        map.put(ksDelete, "ksDelete");
        act.put("ksDelete", aDelete);
        mDelete.setAccelerator(ksDelete);

        KeyStroke ksNewFile = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        AbstractAction aNewFile = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFile(fileBrowserView.getCurrentDirectory(), fileBrowserPanel.getFiles());
            }
        };
        mNewFile = new JMenuItem("New file");
        mNewFile.addActionListener(aNewFile);
        map.put(ksNewFile, "ksNewFile");
        act.put("ksNewFile", aNewFile);
        mNewFile.setAccelerator(ksNewFile);

        KeyStroke ksNewFolder = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        AbstractAction aNewFolder = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFolder(fileBrowserView.getCurrentDirectory(), fileBrowserPanel.getFiles());
            }
        };
        mNewFolder = new JMenuItem("New folder");
        mNewFolder.addActionListener(aNewFolder);
        mNewFolder.setAccelerator(ksNewFolder);
        map.put(ksNewFolder, "ksNewFolder");
        act.put("ksNewFolder", aNewFolder);

        KeyStroke ksCopy = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        AbstractAction aCopy = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard(false);
            }
        };
        mCopy = new JMenuItem("Copy");
        mCopy.addActionListener(aCopy);
        map.put(ksCopy, "ksCopy");
        act.put("ksCopy", aCopy);
        mCopy.setAccelerator(ksCopy);

        KeyStroke ksCopyPath = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        AbstractAction aCopyPath = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyPathToClipboard();
            }
        };
        mCopyPath = new JMenuItem("Copy path");
        mCopyPath.addActionListener(aCopyPath);
        map.put(ksCopyPath, "ksCopyPath");
        act.put("ksCopyPath", aCopyPath);
        mCopyPath.setAccelerator(ksCopyPath);

        KeyStroke ksPaste = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        AbstractAction aPaste = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePaste();
            }
        };
        mPaste = new JMenuItem("Paste");
        mPaste.addActionListener(aPaste);
        map.put(ksPaste, "ksPaste");
        act.put("ksPaste", aPaste);
        mPaste.setAccelerator(ksPaste);

        KeyStroke ksCut = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);
        AbstractAction aCut = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard(true);
            }
        };
        mCut = new JMenuItem("cut");
        mCut.addActionListener(aCut);
        map.put(ksCut, "ksCut");
        act.put("ksCut", aCut);
        mCut.setAccelerator(ksCut);

        mDownload = new JMenuItem("Download selected files");
        mDownload.addActionListener(e -> {
            downloadFiles(fileBrowserPanel.getFileBrowserTable().getSelectedFiles(), fileBrowserView.getCurrentDirectory());
        });

        mUpload = new JMenuItem("Upload here");
        mUpload.addActionListener(e -> {
            try {
                uploadFiles();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void copyToClipboard(boolean cut) {
        FileInfo[] selectedFiles = fileBrowserPanel.getFileBrowserTable().getSelectedFiles();
        DndTransferData transferData = new DndTransferData(fileBrowser.getSFtpConnector().getSessionItemModel().hashCode(), selectedFiles,
                fileBrowserView.getCurrentDirectory(), fileBrowserView.hashCode(), DndTransferData.DndSourceType.SSH);
        transferData.setTransferAction(cut ? DndTransferData.TransferAction.Cut : DndTransferData.TransferAction.Copy);

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DndTransferHandler.DATA_FLAVOR_DATA_FILE};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DndTransferHandler.DATA_FLAVOR_DATA_FILE);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                return transferData;
            }
        }, (a, b) -> {
        });
    }

    private void copyPathToClipboard() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (FileInfo f : fileBrowserPanel.getFileBrowserTable().getSelectedFiles()) {
            if (!first) {
                sb.append("\n");
            }
            sb.append(f.getPath());
            if (first) {
                first = false;
            }
        }
        if (sb.length() > 0) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        }
    }

    private void rename(FileInfo info, String baseFolder) {
        String text = JOptionPane.showInputDialog("Please enter the new name", info.getName());
        if (text != null && text.length() > 0) {
            renameAsync(info.getPath(), PathUtils.combineUnix(PathUtils.getParent(info.getPath()), text), baseFolder);
        }
    }

    private void renameAsync(String oldName, String newName, String baseFolder) {
        animationComponent.start();

        executor.submit(() -> {
            try {
                if (fileOperations.rename(oldName, newName, fileBrowserView.getFileSystem(),
                        fileBrowser.getSFtpConnector(), Utils.decodeString(fileBrowser.getSFtpConnector().getSessionItemModel().getPassword()))) {
                    fileBrowserView.render(baseFolder);
                }
                animationComponent.stop();
            } catch (Exception e) {
                LOG.error(e);
                animationComponent.stop();
            }
        });
    }

    private void delete(FileInfo[] targetList, String baseFolder) {
        animationComponent.start();

        executor.submit(() -> {
            try {
                if (fileOperations.delete(targetList, fileBrowserView.getFileSystem(),
                        fileBrowser.getSFtpConnector(), Utils.decodeString(fileBrowser.getSFtpConnector().getSessionItemModel().getPassword()))) {
                    fileBrowserView.render(baseFolder);
                }
                animationComponent.stop();
            } catch (Exception e) {
                LOG.error(e);
                animationComponent.stop();
            }
        });
    }

    public void newFile(String baseFolder, FileInfo[] files) {
        animationComponent.start();

        executor.submit(() -> {
            try {
                if (fileOperations.newFile(files, fileBrowserView.getFileSystem(), baseFolder,
                        fileBrowser.getSFtpConnector(), Utils.decodeString(fileBrowser.getSFtpConnector().getSessionItemModel().getPassword()))) {
                    fileBrowserView.render(baseFolder);
                }
                animationComponent.stop();
            } catch (Exception e) {
                LOG.error(e);
                animationComponent.stop();
            }
        });
    }

    public void newFolder(String baseFolder, FileInfo[] files) {
        animationComponent.start();

        executor.submit(() -> {
            try {
                if (fileOperations.newFolder(files, baseFolder, fileBrowserView.getFileSystem(),
                        fileBrowser.getSFtpConnector(), Utils.decodeString(fileBrowser.getSFtpConnector().getSessionItemModel().getPassword()))) {
                    fileBrowserView.render(baseFolder);
                }
                animationComponent.stop();
            } catch (Exception e) {
                LOG.error(e);
                animationComponent.stop();
            }
        });
    }

    private void handlePaste() {
        if (Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DndTransferHandler.DATA_FLAVOR_DATA_FILE)) {
            try {
                DndTransferData transferData = (DndTransferData) Toolkit.getDefaultToolkit().getSystemClipboard()
                        .getData(DndTransferHandler.DATA_FLAVOR_DATA_FILE);
                if (transferData != null) {
                    fileBrowserView.handleDrop(transferData);
                }
            } catch (UnsupportedFlavorException | IOException e) {
                LOG.error(e);
            }
        } else {
            DataFlavor[] flavors = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors();
            for (DataFlavor flavor : flavors) {
                if (flavor.isFlavorJavaFileListType()) {
                }
            }
        }
    }

    public void copy(List<FileInfo> files, String targetFolder) {
        animationComponent.start();

        executor.submit(() -> {
            try {
                if (fileOperations.copyTo(fileBrowser.getSFtpConnector(), files, targetFolder,
                        fileBrowserView.getFileSystem(), Utils.decodeString(fileBrowser.getSFtpConnector().getSessionItemModel().getPassword()))) {
                    fileBrowserView.render(targetFolder);
                }
                animationComponent.stop();
            } catch (Exception e) {
                LOG.error(e);
            }
        });
    }

    public void move(List<FileInfo> files, String targetFolder) {
        animationComponent.start();

        executor.submit(() -> {
            try {
                if (fileOperations.moveTo(fileBrowser.getSFtpConnector(), files, targetFolder,
                        fileBrowserView.getFileSystem(), Utils.decodeString(fileBrowser.getSFtpConnector().getSessionItemModel().getPassword()))) {
                    fileBrowserView.render(targetFolder);
                }
                animationComponent.stop();
            } catch (Exception e) {
                LOG.error(e);
            }
        });
    }

    public JPopupMenu createAddressPopup() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mCopyPath = new JMenuItem("Copy path");
        popupMenu.add(mCopyPath);

        mCopyPath.addActionListener(e -> {
            String path = popupMenu.getName();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(path), null);
        });
        return popupMenu;
    }

    private void downloadFiles(FileInfo[] files, String currentDirectory) {
        throw new RuntimeException("Not implemented");
    }

    private void uploadFiles() throws IOException {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setMultiSelectionEnabled(true);
        if (jfc.showOpenDialog(SwingUtilities.getWindowAncestor(fileBrowser)) == JFileChooser.APPROVE_OPTION) {
            File[] files = jfc.getSelectedFiles();
            if (files.length > 0) {
                List<FileInfo> list = new ArrayList<>(0);

                try (LocalFileSystem localFileSystem = new LocalFileSystem()) {
                    for (File file : files) {
                        FileInfo fileInfo = localFileSystem.getInfo(file.getAbsolutePath());
                        list.add(fileInfo);
                    }
                }
                DndTransferData uploadData = new DndTransferData(0, list.toArray(new FileInfo[0]), files[0].getParent(),
                        0, DndTransferData.DndSourceType.LOCAL);
                fileBrowserView.handleDrop(uploadData);
            }
        }
    }

    public boolean createMenu(JPopupMenu popup, FileInfo[] files) {
        popup.removeAll();
        int selectionCount = files.length;
        int count = 0;
        count += createBuiltInItems1(selectionCount, popup, files);
        count += createBuiltInItems2(selectionCount, popup, files);
        return count > 0;
    }

    private boolean hasSupportedContentOnClipboard() {
        boolean ret = (Toolkit.getDefaultToolkit().getSystemClipboard()
                .isDataFlavorAvailable(DndTransferHandler.DATA_FLAVOR_DATA_FILE)
                || Toolkit.getDefaultToolkit().getSystemClipboard()
                .isDataFlavorAvailable(DataFlavor.javaFileListFlavor));
        if (!ret)
            LOG.debug("Nothing on clipboard");
        return ret;
    }

    private int createBuiltInItems1(int selectionCount, JPopupMenu popup, FileInfo[] selectedFiles) {
        int count = 0;
        if (selectionCount > 0) {
            popup.add(mCut);
            popup.add(mCopy);
            popup.add(mCopyPath);
            popup.add(mDownload);
            count += 3;
        }

        if (hasSupportedContentOnClipboard()) {
            popup.add(mPaste);
        }

        if (selectionCount == 1) {
            popup.add(mRename);
            count++;
        }

        return count;
    }

    private int createBuiltInItems2(int selectionCount, JPopupMenu popup, FileInfo[] selectedFiles) {
        int count = 0;
        if (selectionCount > 0) {
            popup.add(mDelete);
            count += 3;
        }

        if (selectionCount < 1) {
            popup.add(mNewFolder);
            popup.add(mNewFile);
            count += 2;
        }

        if (selectionCount < 1 || (selectionCount == 1
                && (selectedFiles[0].getType() == FileType.File || selectedFiles[0].getType() == FileType.FileLink))) {
            popup.add(mUpload);
            count += 1;
        }

        count++;

        return count;
    }

}
