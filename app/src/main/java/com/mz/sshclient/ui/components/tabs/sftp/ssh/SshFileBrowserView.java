package com.mz.sshclient.ui.components.tabs.sftp.ssh;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ssh.sftp.SFtpConnector;
import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.local.LocalFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.sftp.SshFileSystem;
import com.mz.sshclient.ui.components.tabs.sftp.AbstractFileBrowserView;
import com.mz.sshclient.ui.components.tabs.sftp.FileBrowser;
import com.mz.sshclient.ui.components.tabs.sftp.view.AddressBar;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferData;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferHandler;
import com.mz.sshclient.utils.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SshFileBrowserView extends AbstractFileBrowserView {
    
    private static final Logger LOG = LogManager.getLogger(SshFileBrowserView.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final SshMenuHandler menuHandler;
    private final JPopupMenu addressPopup;
    private final DndTransferHandler transferHandler;

    public SshFileBrowserView(FileBrowser fileBrowser, String initialPath, PanelOrientation orientation) {
        super(orientation, fileBrowser);
        this.menuHandler = new SshMenuHandler(fileBrowser, this);
        this.menuHandler.initMenuHandler(this.folderView);
        this.transferHandler = new DndTransferHandler(this.folderView, this.fileBrowser.getSFtpConnector().getSessionItemModel(), this,
                DndTransferData.DndSourceType.SSH, this.fileBrowser);
        this.folderView.setTransferHandler(transferHandler);
        this.folderView.setFolderViewTransferHandler(transferHandler);
        this.addressPopup = menuHandler.createAddressPopup();
        if (initialPath == null) {
            this.path = this.fileBrowser.getSFtpConnector().getSessionItemModel().getRemoteFolder();
            if (this.path != null && this.path.trim().length() < 1) {
                this.path = null;
            }
        } else {
            this.path = initialPath;
        }

        this.render(path, true);
    }

    public void createAddressBar() {
        addressBar = new AddressBar('/', e -> {
            String selectedPath = e.getActionCommand();
            addressPopup.setName(selectedPath);
            MouseEvent me = (MouseEvent) e.getSource();
            addressPopup.show(me.getComponent(), me.getX(), me.getY());
        });
        addressBar.switchToPathBar();
    }

    @Override
    public String toString() {
        return this.fileBrowser.getSFtpConnector().getSessionItemModel().getName()
                + (this.path == null || this.path.length() < 1 ? "" : " [" + this.path + "]");
    }

    private String trimPath(String path) {
        if (path.equals("/"))
            return path;
        if (path.endsWith("/")) {
            String trim = path.substring(0, path.length() - 1);
            System.out.println("Trimmed path: " + trim);
            return trim;
        }
        return path;
    }

    private void renderDirectory(final String path, final boolean fromCache) throws Exception {
        List<FileInfo> list = null;
        if (fromCache) {
            list = fileBrowser.getSSHDirectoryCache().get(trimPath(path));
        }
        if (list == null) {
            list = fileBrowser.getSSHFileSystem().list(path);
            if (list != null) {
                fileBrowser.getSSHDirectoryCache().put(trimPath(path), list);
            }
        }
        if (list != null) {
            final List<FileInfo> list2 = list;
            SwingUtilities.invokeLater(() -> {
                addressBar.setText(path);
                folderView.setItems(list2);
                int tc = list2.size();
                String text = String.format("Total %d remote file(s)", tc);
                fileBrowser.updateRemoteStatus(text);
            });
        }
    }

    @Override
    public void render(String path, boolean useCache) {
        this.path = path;

        executor.submit(() -> {
            while (!fileBrowser.getSFtpConnector().isSessionClosed()) {
                try {
                    if (path == null) {
                        SshFileSystem sshfs = this.fileBrowser.getSSHFileSystem();
                        this.path = sshfs.getHome();
                    }
                    renderDirectory(this.path, useCache);
                    break;
                } catch (SshOperationCanceledException e) {
                    LOG.error(e);
                    break;
                } catch (Exception e) {
                    LOG.error(e);

                    if (this.fileBrowser.getSFtpConnector().isSessionClosed()) {
                        return;
                    }

                    this.fileBrowser.reconnect();

                    if (JOptionPane.showConfirmDialog(null,
                            "Unable to connect to server " + this.fileBrowser.getSFtpConnector().getSessionItemModel().getName() + " at "
                                    + this.fileBrowser.getSFtpConnector().getSessionItemModel().getHost()
                                    + (e.getMessage() != null ? "\n\nReason: " + e.getMessage() : "\n")
                                    + "\n\nDo you want to retry?") == JOptionPane.YES_OPTION) {
                        continue;
                    }
                    break;
                }
            }
        });
    }

    @Override
    public void render(String path) {
        this.render(path, false);
    }

    @Override
    public void openApp(FileInfo file) {
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
    public void install(JComponent c) {

    }

    @Override
    public boolean createMenu(JPopupMenu popup, FileInfo[] files) {
        if (this.path == null) {
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
                    && sessionHashCode == this.fileBrowser.getSFtpConnector().getSessionItemModel().hashCode()) {
                sourceFs = this.fileBrowser.getSSHFileSystem();
            }

            if (sourceFs instanceof LocalFileSystem) {
                FileBrowser.ResponseHolder holder = new FileBrowser.ResponseHolder();
                if (!this.fileBrowser.selectTransferModeAndConflictAction(holder)) {
                    return false;
                }
                IFileSystem targetFs = this.fileBrowser.getSSHFileSystem();
                this.fileBrowser.newFileTransfer(sourceFs, targetFs, transferData.getFiles(), this.path,
                        this.hashCode(), holder.conflictAction, null);
            } else if (sourceFs instanceof SshFileSystem && (sourceFs == this.fileBrowser.getSSHFileSystem())) {
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
            } else if (sourceFs instanceof SshFileSystem
                    && (transferData.getSourceType() == DndTransferData.DndSourceType.SFTP)) {
            }
            return true;
        } catch (Exception e) {
            LOG.error(e);
            return false;
        }
    }

    public IFileSystem getFileSystem() throws Exception {
        return this.fileBrowser.getSSHFileSystem();
    }

    public SFtpConnector getSshClient() throws Exception {
        return this.fileBrowser.getSFtpConnector();
    }

    @Override
    public TransferHandler getTransferHandler() {
        return transferHandler;
    }

    public String getHostText() {
        return this.fileBrowser.getSFtpConnector().getSessionItemModel().getName();
    }

    public String getPathText() {
        return (this.path == null || this.path.length() < 1 ? "" : this.path);
    }

}
