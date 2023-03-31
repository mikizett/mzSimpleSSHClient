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

        menuHandler = new LocalMenuHandler(fileBrowser, this);
        menuHandler.initMenuHandler(fileBrowserPanel);

        DndTransferHandler transferHandler = new DndTransferHandler(fileBrowserPanel, null, this, DndTransferData.DndSourceType.LOCAL, this.fileBrowser);
        fileBrowserPanel.setTransferHandler(transferHandler);
        fileBrowserPanel.setFolderViewTransferHandler(transferHandler);

        addressPopup = menuHandler.createAddressPopup();

        if (fileBrowser.getSFtpConnector().getSessionItemModel().getLocalFolder() != null &&
                fileBrowser.getSFtpConnector().getSessionItemModel().getLocalFolder().trim().length() > 1) {
            path = fileBrowser.getSFtpConnector().getSessionItemModel().getLocalFolder();
        } else if (initialPath != null) {
            path = initialPath;
        }

        executor.submit(() -> {
            try {
                fs = new LocalFileSystem();
                // validate if local path exists, if not set the home path
                if (path == null || Files.notExists(Paths.get(path)) || !Files.isDirectory(Paths.get(path))) {
                    LOG.debug("The file path doesn't exists " + path);
                    LOG.debug("Setting to " + fs.getHome());

                    path = fs.getHome();
                }
                List<FileInfo> list = fs.list(path);
                SwingUtilities.invokeLater(() -> {
                    addressBar.setText(path);
                    fileBrowserPanel.setItems(list);
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
        return "Local files [" + path + "]";
    }

    @Override
    public String getHostText() {
        return "Local files";
    }

    @Override
    public String getPathText() {
        return (path == null || path.length() < 1 ? "" : path);
    }

    @Override
    public void render(String path, boolean useCache) {
        render(path);
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
                    fileBrowserPanel.setItems(list);
                });
            } catch (Exception e) {
                LOG.error(e);
            }
        });
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
