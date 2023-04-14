package com.mz.sshclient.ui.components.tabs.sftp;

import com.mz.sshclient.Constants;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.HostKeyVerifier;
import com.mz.sshclient.ssh.SshClient;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ssh.exceptions.SshConnectionException;
import com.mz.sshclient.ssh.exceptions.SshDisconnectException;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ssh.sftp.ConflictAction;
import com.mz.sshclient.ssh.sftp.SFtpConnector;
import com.mz.sshclient.ssh.sftp.TransferMode;
import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.sftp.SshFileSystem;
import com.mz.sshclient.ui.OpenedSshSessions;
import com.mz.sshclient.ui.components.common.animation.ConnectAnimationComponent;
import com.mz.sshclient.ui.components.tabs.sftp.local.LocalIFileBrowserView;
import com.mz.sshclient.ui.components.tabs.sftp.ssh.SshFileBrowserView;
import com.mz.sshclient.ui.components.tabs.sftp.transfer.FileTransfer;
import com.mz.sshclient.ui.components.tabs.sftp.transfer.IFileTransferProgress;
import com.mz.sshclient.ui.components.tabs.sftp.transfer.TransferProgressPanel;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferData;
import com.mz.sshclient.ui.components.tabs.terminal.HostKeyVerificationCallback;
import com.mz.sshclient.ui.components.tabs.terminal.InteractiveResponseProvider;
import com.mz.sshclient.ui.components.tabs.terminal.PasswordRetryCallback;
import com.mz.sshclient.ui.components.tabs.terminal.PrivateKeyPasswordFinderCallback;
import com.mz.sshclient.ui.config.AppConfig;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FileBrowser extends JPanel {

    private static final Logger LOG = LogManager.getLogger(FileBrowser.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ConnectAnimationComponent anime = new ConnectAnimationComponent(mzSimpleSshClientMain.MAIN_FRAME, "Connecting sFTP ");

    private final TransferProgressPanel progressPanel = new TransferProgressPanel();

    private final List<AbstractIFileBrowserView> viewList = new ArrayList<>(0);
    private final Map<String, List<FileInfo>> sshDirCache = new HashMap<>(0);

    private SFtpConnector sFtpConnector;

    private final JSplitPane horizontalSplitter;
    private FileBrowserContentComponent leftComponent;
    private LocalIFileBrowserView localFileBrowserView;

    private FileBrowserContentComponent rightComponent;
    private SshFileBrowserView sshFileBrowserView;

    private FileTransfer ongoingFileTransfer;

    public FileBrowser(final SFtpConnector sFtpConnector) {
        super(new BorderLayout());
        this.sFtpConnector = sFtpConnector;

        horizontalSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitter.setResizeWeight(0.5);
        horizontalSplitter.setDividerSize(5);

        add(horizontalSplitter, BorderLayout.CENTER);
    }

    private void initFileBrowserComponents() {
        localFileBrowserView = new LocalIFileBrowserView(this, SystemUtils.USER_HOME, AbstractIFileBrowserView.PanelOrientation.LEFT);
        leftComponent = new FileBrowserContentComponent("Local Filesystem", localFileBrowserView);

        sshFileBrowserView = new SshFileBrowserView(this, null, AbstractIFileBrowserView.PanelOrientation.RIGHT);
        rightComponent = new FileBrowserContentComponent("Remote Filesystem", sshFileBrowserView);

        horizontalSplitter.setLeftComponent(leftComponent);
        horizontalSplitter.setRightComponent(rightComponent);
    }

    public Map<String, List<FileInfo>> getSSHDirectoryCache() {
        return sshDirCache;
    }

    public SshFileSystem getSSHFileSystem() {
        return sFtpConnector.getSshFs();
    }

    public SFtpConnector getSFtpConnector() {
        return sFtpConnector;
    }

    public boolean isSFtpConnected() {
        return sFtpConnector.getSshClient().isConnected();
    }

    public void reconnect() {
        final SessionItemModel sessionItemModel = sFtpConnector.getSessionItemModel().clone(false);
        sFtpConnector.close();

        anime.start();

        try {
            final SshClient sshClient = createSshClient(sessionItemModel);
            sshClient.connect();

            final SshTtyConnector sshTtyConnector = new SshTtyConnector(sshClient);

            sFtpConnector = new SFtpConnector(sessionItemModel, sshClient);

            OpenedSshSessions.addSshSession(sessionItemModel, sshTtyConnector, sFtpConnector);

            anime.stop();
        } catch (SshConnectionException | SshDisconnectException | SshOperationCanceledException e) {
            anime.stop();

            LOG.error("Could not reconnect", e);
            MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
        }
    }

    public void connectSFtp() {
        anime.start();

        try {
            sFtpConnector.getSshClient().connect();

            SwingUtilities.invokeLater(() -> {
                initFileBrowserComponents();
                invalidate();
                revalidate();
                anime.stop();
            });
        } catch (SshConnectionException | SshDisconnectException | SshOperationCanceledException e) {
            anime.stop();

            LOG.error("Could not connect", e);
            MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
        }
    }

    public boolean selectTransferModeAndConflictAction(ResponseHolder holder) {
        holder.transferMode = TransferMode.NORMAL;
        holder.conflictAction = ConflictAction.AUTORENAME;
        return true;
    }

    public boolean handleLocalDrop(DndTransferData transferData, SessionItemModel info, IFileSystem currentFileSystem, String currentPath) {
        try {
            ResponseHolder holder = new ResponseHolder();

            if (!selectTransferModeAndConflictAction(holder)) {
                return false;
            }

            int sessionHashCode = transferData.getInfo();
            if (sessionHashCode == 0) {
                return true;
            }

            if (info != null && info.hashCode() == sessionHashCode) {
                IFileSystem sourceFs = getSSHFileSystem();
                if (sourceFs == null) {
                    return false;
                }
                IFileSystem targetFs = currentFileSystem;
                newFileTransfer(
                        sourceFs,
                        targetFs,
                        transferData.getFiles(),
                        currentPath,
                        holder.conflictAction
                );
            }
            return true;
        } catch (Exception e) {
            LOG.error(e);
            return false;
        }
    }

    public void newFileTransfer(
            IFileSystem sourceFs,
            IFileSystem targetFs,
            FileInfo[] files,
            String targetFolder,
            ConflictAction defaultConflictAction
    ) {
        ongoingFileTransfer = new FileTransfer(sourceFs, targetFs, files, targetFolder,
                new IFileTransferProgress() {
                    @Override
                    public void progress(
                            long processedBytes,
                            long totalBytes,
                            long processedCount,
                            long totalCount,
                            FileTransfer fileTransfer
                    ) {
                        SwingUtilities.invokeLater(() -> {
                            if (totalBytes == 0) {
                                setTransferProgress(0);
                            } else {
                                setTransferProgress((int) ((processedBytes * 100) / totalBytes));
                            }
                        });
                    }

                    @Override
                    public void init(long totalSize, long files, FileTransfer fileTransfer) {
                    }

                    @Override
                    public void error(final String cause, final FileTransfer fileTransfer) {
                        SwingUtilities.invokeLater(() -> {
                            endFileTransfer();
                            if (!sFtpConnector.isSessionClosed()) {
                                MessageDisplayUtil.showErrorMessage("Operation failed!\n\n" + cause);
                            }
                            reloadView();
                        });
                    }

                    @Override
                    public void done(FileTransfer fileTransfer) {
                        SwingUtilities.invokeLater(() -> {
                            endFileTransfer();
                            reloadView();
                        });
                    }
                }, defaultConflictAction);
        startFileTransferModal(e -> ongoingFileTransfer.close());
        executor.submit(ongoingFileTransfer);
    }

    public void registerForViewNotification(AbstractIFileBrowserView view) {
        this.viewList.add(view);
    }

    public void unRegisterForViewNotification(AbstractIFileBrowserView view) {
        this.viewList.remove(view);
    }

    private void reloadView() {
        localFileBrowserView.reload();
        sshFileBrowserView.reload();
    }

    private void startFileTransferModal(Consumer<Boolean> stopCallback) {
        progressPanel.setStopCallback(stopCallback);
        progressPanel.clear();
        progressPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void setTransferProgress(int progress) {
        progressPanel.setProgress(progress);
    }

    private void endFileTransfer() {
        progressPanel.setVisible(false);
        revalidate();
        repaint();
    }

    private HostKeyVerifier createHostKeyVerifier() {
        HostKeyVerifier hostKeyVerifier = null;
        try {
            final File hostKeyFile = new File(AppConfig.getKnownHostsLocation());
            hostKeyVerifier  = new HostKeyVerifier(hostKeyFile, new HostKeyVerificationCallback());
        } catch (IOException e) {
            LOG.error("Could not load host key verifier <" + Constants.KNOWN_HOSTS_FILE_NAME + ">" , e);
        }
        return hostKeyVerifier;
    }

    private SshClient createSshClient(final SessionItemModel item) {
        final HostKeyVerifier hostKeyVerifier = createHostKeyVerifier();
        final PrivateKeyPasswordFinderCallback privateKeyPasswordFinderCallback = new PrivateKeyPasswordFinderCallback(item);
        final InteractiveResponseProvider interactiveResponseProvider = new InteractiveResponseProvider(item);
        final PasswordRetryCallback passwordRetryCallback = new PasswordRetryCallback(item);

        final SshClient sshClient = new SshClient(
                item,
                hostKeyVerifier,
                privateKeyPasswordFinderCallback,
                interactiveResponseProvider,
                passwordRetryCallback
        );

        return sshClient;
    }

    public static class ResponseHolder {
        public TransferMode transferMode;
        public ConflictAction conflictAction;
    }

}
