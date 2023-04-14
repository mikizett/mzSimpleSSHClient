package com.mz.sshclient.ui.components.tabs.sftp.ssh;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.local.LocalFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.sftp.SshFileSystem;
import com.mz.sshclient.ui.components.tabs.sftp.AbstractIFileBrowserView;
import com.mz.sshclient.ui.components.tabs.sftp.FileBrowser;
import com.mz.sshclient.ui.components.tabs.sftp.view.AddressBar;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferData;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferHandler;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import com.mz.sshclient.utils.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SshFileBrowserView extends AbstractIFileBrowserView {
    
    private static final Logger LOG = LogManager.getLogger(SshFileBrowserView.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final SshMenuHandler menuHandler;
    private final JPopupMenu addressPopup;
    private final DndTransferHandler transferHandler;

    public SshFileBrowserView(FileBrowser fileBrowser, String initialPath, PanelOrientation orientation) {
        super(orientation, fileBrowser);

        menuHandler = new SshMenuHandler(fileBrowser, this);
        menuHandler.initMenuHandler(fileBrowserPanel);

        transferHandler = new DndTransferHandler(
                fileBrowserPanel,
                fileBrowser.getSFtpConnector().getSessionItemModel(),
                this,
                DndTransferData.DndSourceType.SSH,
                fileBrowser
        );

        fileBrowserPanel.setTransferHandler(transferHandler);
        fileBrowserPanel.setFolderViewTransferHandler(transferHandler);

        addressPopup = menuHandler.createAddressPopup();

        if (initialPath == null) {
            path = fileBrowser.getSFtpConnector().getSessionItemModel().getRemoteFolder();
            if (path != null && path.trim().length() < 1) {
                path = null;
            }
        } else {
            path = initialPath;
        }

        render(path, true);
    }

    public void createAddressBar() {
        addressBar = new AddressBar(this, e -> {
            String selectedPath = e.getActionCommand();
            addressPopup.setName(selectedPath);
            MouseEvent me = (MouseEvent) e.getSource();
            addressPopup.show(me.getComponent(), me.getX(), me.getY());
        });
        addressBar.switchToPathBar();
    }

    @Override
    public String toString() {
        return fileBrowser.getSFtpConnector().getSessionItemModel().getName()
                + (path == null || path.length() < 1 ? "" : " [" + path + "]");
    }

    private String trimPath(String path) {
        if (path.equals("/"))
            return path;
        if (path.endsWith("/")) {
            String trim = path.substring(0, path.length() - 1);
            return trim;
        }
        return path;
    }

    private void renderDirectory(final String path) throws Exception {
        final List<FileInfo> list = getRemoteFileRoots(path);
        if (list != null) {
            SwingUtilities.invokeLater(() -> {
                addressBar.setText(path);
                fileBrowserPanel.setItems(list);
            });
        }
    }

    public List<FileInfo> getRemoteFileRoots(final String path) throws Exception {
        List<FileInfo> list = fileBrowser.getSSHDirectoryCache().get(trimPath(path));
        if (list == null) {
            list = fileBrowser.getSSHFileSystem().list(path);
            if (list != null) {
                fileBrowser.getSSHDirectoryCache().put(trimPath(path), list);
            }
        }
        return list;
    }

    @Override
    public void render(String path, boolean useCache) {
        this.path = path;

        executor.submit(() -> {
            while (!fileBrowser.getSFtpConnector().isSessionClosed()) {
                try {
                    if (this.path == null) {
                        SshFileSystem sshfs = fileBrowser.getSSHFileSystem();
                        this.path = sshfs.getHome();
                    }
                    renderDirectory(this.path);
                    break;
                } catch (SshOperationCanceledException e) {
                    LOG.error(e);
                    break;
                } catch (Exception e) {
                    LOG.error(e);

                    if (fileBrowser.getSFtpConnector().isSessionClosed()) {
                        return;
                    }

                    fileBrowser.reconnect();

                    int answer = MessageDisplayUtil.showYesNoConfirmDialog(
                            "Unable to connect to server " + fileBrowser.getSFtpConnector().getSessionItemModel().getName() + " at "
                            + fileBrowser.getSFtpConnector().getSessionItemModel().getHost()
                            + (e.getMessage() != null ? "\n\nReason: " + e.getMessage() : "\n")
                            + "\n\nDo you want to retry?",
                            "Retry?"
                    );
                    if (answer == JOptionPane.YES_OPTION) {
                        continue;
                    }
                    break;
                }
            }
        });
    }

    @Override
    public void render(String path) {
        render(path, false);
    }

    protected void up() {
        if (path != null) {
            String parent = PathUtils.getParent(path);
            addBack(path);
            render(parent, true);
        }
    }

    protected void home() {
        addBack(path);
        render(null, true);
    }

    @Override
    public boolean createMenu(JPopupMenu popup, FileInfo[] files) {
        if (path == null) {
            return false;
        }
        return menuHandler.createMenu(popup, files);
    }

    public boolean handleDrop(DndTransferData transferData) {
        try {
            int sessionHashCode = transferData.getInfo();
            IFileSystem sourceFs = null;
            if (sessionHashCode == 0 && transferData.getSourceType() == DndTransferData.DndSourceType.LOCAL) {
                sourceFs = new LocalFileSystem();
            } else if (transferData.getSourceType() == DndTransferData.DndSourceType.SSH
                    && sessionHashCode == fileBrowser.getSFtpConnector().getSessionItemModel().hashCode()) {
                sourceFs = fileBrowser.getSSHFileSystem();
            }

            if (sourceFs instanceof LocalFileSystem) {
                FileBrowser.ResponseHolder holder = new FileBrowser.ResponseHolder();
                if (!fileBrowser.selectTransferModeAndConflictAction(holder)) {
                    return false;
                }
                IFileSystem targetFs = fileBrowser.getSSHFileSystem();

                this.fileBrowser.newFileTransfer(
                        sourceFs,
                        targetFs,
                        transferData.getFiles(),
                        path,
                        holder.conflictAction
                );

            } else if (sourceFs instanceof SshFileSystem && (sourceFs == fileBrowser.getSSHFileSystem())) {
                if (transferData.getFiles().length > 0) {
                    FileInfo fileInfo = transferData.getFiles()[0];
                    String parent = PathUtils.getParent(fileInfo.getPath());
                    if (!parent.endsWith("/")) {
                        parent += "/";
                    }
                    String pwd = this.getCurrentDirectory();
                    if (!pwd.endsWith("/")) {
                        pwd += "/";
                    }
                    if (parent.equals(pwd)) {
                        JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Source and target directory is same!");
                        return false;
                    }
                }

                if (transferData.getTransferAction() == DndTransferData.TransferAction.Copy) {
                    menuHandler.copy(Arrays.asList(transferData.getFiles()), getCurrentDirectory());
                } else {
                    menuHandler.move(Arrays.asList(transferData.getFiles()), getCurrentDirectory());
                }
            }
            return true;
        } catch (Exception e) {
            LOG.error(e);
        }
        return false;
    }

    public IFileSystem getFileSystem() throws Exception {
        return fileBrowser.getSSHFileSystem();
    }

    @Override
    public TransferHandler getTransferHandler() {
        return transferHandler;
    }

    public String getHostText() {
        return fileBrowser.getSFtpConnector().getSessionItemModel().getName();
    }

    public String getPathText() {
        return (path == null || path.length() < 1 ? "" : path);
    }

}
