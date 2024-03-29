package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ssh.sftp.filesystem.local.LocalFileSystem;
import com.mz.sshclient.ui.components.tabs.sftp.AbstractIFileBrowserView;
import com.mz.sshclient.ui.components.tabs.sftp.FileBrowser;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DndTransferHandler extends TransferHandler implements Transferable {

    private static final Logger LOG = LogManager.getLogger(DndTransferHandler.class);

    private static final String uuid = UUID.randomUUID().toString();

    public static final DataFlavor DATA_FLAVOR_DATA_FILE = new DataFlavor(DndTransferData.class, "data-file");
    public static final DataFlavor DATA_FLAVOR_FILE_LIST = DataFlavor.javaFileListFlavor;
    private final FileBrowserPanel fileBrowserPanel;
    private final SessionItemModel info;
    private final AbstractIFileBrowserView fileBrowserView;
    private DndTransferData transferData;
    private final DndTransferData.DndSourceType sourceType;
    private Win32DragHandler win32DragHandler;
    private File tempDir;
    private final FileBrowser fileBrowser;

    public DndTransferHandler(
            FileBrowserPanel fileBrowserPanel,
            SessionItemModel info,
            AbstractIFileBrowserView fileBrowserView,
            DndTransferData.DndSourceType sourceType,
            FileBrowser fileBrowser
    ) {
        this.fileBrowserPanel = fileBrowserPanel;
        this.fileBrowser = fileBrowser;
        this.info = info;
        this.fileBrowserView = fileBrowserView;
        this.sourceType = sourceType;
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        if (info != null) {
            if (SystemUtils.IS_OS_WINDOWS) {
                try {
                    tempDir = Files.createTempDirectory(uuid).toFile();
                    win32DragHandler = new Win32DragHandler();
                    win32DragHandler.listenForDrop(tempDir.getName(), file ->
                            fileBrowser.handleLocalDrop(transferData, info, new LocalFileSystem(), file.getParent())
                    );
                } catch (IOException e1) {
                    LOG.error(e);
                }
            }
        }

        DndTransferData data = new DndTransferData(
                info == null ? 0 : info.hashCode(),
                fileBrowserPanel.getFileBrowserTable().getSelectedFiles(),
                fileBrowserView.getCurrentDirectory(),
                fileBrowserView.hashCode(), sourceType
        );
        transferData = data;
        super.exportAsDrag(comp, e, action);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        boolean isDataFile = false;
        boolean isJavaFileList = false;

        for (DataFlavor f : support.getDataFlavors()) {
            if (f.isFlavorJavaFileListType()) {
                isJavaFileList = info != null;
            }
            if (DATA_FLAVOR_DATA_FILE.equals(f)) {
                isDataFile = true;
            }
        }

        try {
            if (isDataFile) {
                if (support.isDataFlavorSupported(DATA_FLAVOR_DATA_FILE)) {
                    return (support.getTransferable().getTransferData(DATA_FLAVOR_DATA_FILE) instanceof DndTransferData);
                }
            } else if (isJavaFileList) {
                return true;
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        if (this.win32DragHandler != null) {
            this.win32DragHandler.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        boolean isDataFile = false;
        boolean isJavaFileList = false;

        for (DataFlavor f : info.getDataFlavors()) {
            if (f.isFlavorJavaFileListType()) {
                isJavaFileList = this.info != null;
            }
            if (DATA_FLAVOR_DATA_FILE.equals(f)) {
                isDataFile = true;
            }
        }

        Transferable t = info.getTransferable();

        if (isDataFile) {
            try {
                DndTransferData transferData = (DndTransferData) t.getTransferData(DATA_FLAVOR_DATA_FILE);
                return fileBrowserView.handleDrop(transferData);
            } catch (UnsupportedFlavorException e) {
                LOG.error(e);
            } catch (IOException e) {
                LOG.error(e);
            }
        } else if (isJavaFileList) {
            try {
                List<File> fileList = ((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor));
                if (fileList != null) {
                    FileInfo[] infoArr = new FileInfo[fileList.size()];
                    int c = 0;
                    for (File file : fileList) {

                        if (file.getName().startsWith(uuid)) {
                            return false;
                        }

                        Path p = file.toPath();
                        BasicFileAttributes attrs = null;
                        try {
                            attrs = Files.readAttributes(p, BasicFileAttributes.class);
                        } catch (IOException e) {
                            LOG.error(e);
                        }
                        FileInfo fileInfo = new FileInfo(
                                file.getName(),
                                file.getAbsolutePath(),
                                file.length(),
                                file.isDirectory() ? FileType.Directory : FileType.File,
                                file.lastModified(),
                                -1,
                                LocalFileSystem.PROTOCOL_LOCAL_FILE,
                                "",
                                attrs != null ? attrs.creationTime().toMillis() : file.lastModified(),
                                "",
                                file.isHidden()
                        );
                        infoArr[c++] = fileInfo;
                    }

                    DndTransferData data = new DndTransferData(
                            0,
                            infoArr,
                            fileBrowserView.getCurrentDirectory(),
                            fileBrowserView.hashCode(),
                            DndTransferData.DndSourceType.LOCAL
                    );
                    return fileBrowserView.handleDrop(data);
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return false;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR_DATA_FILE, DATA_FLAVOR_FILE_LIST};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (info != null) {
            return DATA_FLAVOR_DATA_FILE.equals(flavor) || DATA_FLAVOR_FILE_LIST.equals(flavor);
        } else {
            return DATA_FLAVOR_DATA_FILE.equals(flavor);
        }
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (DATA_FLAVOR_DATA_FILE.equals(flavor)) {
            return this.transferData;
        }

        if (DATA_FLAVOR_FILE_LIST.equals(flavor)) {
            if (SystemUtils.IS_OS_WINDOWS && tempDir != null) {
                return Arrays.asList(tempDir);
            }
        }
        return null;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        return this;
    }

}
