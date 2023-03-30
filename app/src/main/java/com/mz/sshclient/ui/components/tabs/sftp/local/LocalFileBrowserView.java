package com.mz.sshclient.ui.components.tabs.sftp.local;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.local.LocalFileSystem;
import com.mz.sshclient.ui.components.tabs.sftp.AbstractFileBrowserView;
import com.mz.sshclient.ui.components.tabs.sftp.FileBrowser;
import com.mz.sshclient.ui.components.tabs.sftp.view.AddressBar;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferData;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalFileBrowserView extends AbstractFileBrowserView {

    private static final Logger LOG = LogManager.getLogger(LocalFileBrowserView.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LocalMenuHandler menuHandler;
    private final JPopupMenu addressPopup;
    private LocalFileSystem fs;

    public LocalFileBrowserView(FileBrowser fileBrowser, String initialPath, PanelOrientation orientation) {
        super(orientation, fileBrowser);
        this.menuHandler = new LocalMenuHandler(fileBrowser, this);
        this.menuHandler.initMenuHandler(this.folderView);
        DndTransferHandler transferHandler = new DndTransferHandler(this.folderView, null, this, DndTransferData.DndSourceType.LOCAL, this.fileBrowser);
        this.folderView.setTransferHandler(transferHandler);
        this.folderView.setFolderViewTransferHandler(transferHandler);
        this.addressPopup = menuHandler.createAddressPopup();

        if (this.fileBrowser.getSFtpConnector().getSessionItemModel().getLocalFolder() != null &&
                this.fileBrowser.getSFtpConnector().getSessionItemModel().getLocalFolder().trim().length() > 1) {
            this.path = fileBrowser.getSFtpConnector().getSessionItemModel().getLocalFolder();
        } else if (initialPath != null) {
            this.path = initialPath;
        }

        executor.submit(() -> {
            try {
                this.fs = new LocalFileSystem();
                //Validate if local path exists, if not set the home path
                if (this.path == null || Files.notExists(Paths.get(this.path)) || !Files.isDirectory(Paths.get(this.path))) {
                    LOG.debug("The file path doesn't exists " + this.path);
                    LOG.debug("Setting to " + fs.getHome());

                    path = fs.getHome();
                }
                List<FileInfo> list = fs.list(path);
                SwingUtilities.invokeLater(() -> {
                    addressBar.setText(path);
                    folderView.setItems(list);
                });
            } catch (Exception e) {
                LOG.error(e);
            }
        });
    }

    public void createAddressBar() {
        addressBar = new AddressBar(File.separatorChar, e -> {
            String selectedPath = e.getActionCommand();
            addressPopup.setName(selectedPath);
            MouseEvent me = (MouseEvent) e.getSource();
            addressPopup.show(me.getComponent(), me.getX(), me.getY());
        });
        addressBar.switchToPathBar();
    }

    @Override
    public String toString() {
        return "Local files [" + this.path + "]";
    }

    public String getHostText() {
        return "Local files";
    }

    public String getPathText() {
        return (this.path == null || this.path.length() < 1 ? "" : this.path);
    }

    @Override
    public void render(String path, boolean useCache) {
        this.render(path);
    }

    @Override
    public void render(String path) {
        this.path = path;

        executor.submit(() -> {
            try {
                if (this.path == null) {
                    this.path = fs.getHome();
                }
                List<FileInfo> list = fs.list(this.path);
                SwingUtilities.invokeLater(() -> {
                    addressBar.setText(this.path);
                    folderView.setItems(list);
                    int tc = list.size();
                    String text = String.format("Total %d remote file(s)", tc);
                    fileBrowser.updateRemoteStatus(text);
                });
            } catch (Exception e) {
                LOG.error(e);
            }
        });
    }

    @Override
    public void openApp(FileInfo file) {
    }

    @Override
    public boolean createMenu(JPopupMenu popup, FileInfo[] files) {
        menuHandler.createMenu(popup, files);
        return true;
    }

    protected void up() {
        String s = new File(path).getParent();
        if (s != null) {
            addBack(path);
            render(s);
        }
    }

    protected void home() {
        addBack(path);
        render(null);
    }

    @Override
    public void install(JComponent c) {
    }

    public boolean handleDrop(DndTransferData transferData) {
        if (transferData.getSource() == this.hashCode()) {
            return false;
        }
        return this.fileBrowser.handleLocalDrop(transferData, fileBrowser.getSFtpConnector().getSessionItemModel(), this.fs, this.path);
    }

    @Override
    public IFileSystem getFileSystem() {
        return new LocalFileSystem();
    }

}
