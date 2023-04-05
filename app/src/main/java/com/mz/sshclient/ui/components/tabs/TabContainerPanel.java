package com.mz.sshclient.ui.components.tabs;

import com.mz.sshclient.Constants;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.events.ConnectSshEvent;
import com.mz.sshclient.services.events.listener.ISshConnectionListener;
import com.mz.sshclient.services.interfaces.ISshConnectionObservableService;
import com.mz.sshclient.ssh.HostKeyVerifier;
import com.mz.sshclient.ssh.IClosedSshConnectionCallback;
import com.mz.sshclient.ssh.SshClient;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ssh.exceptions.SshConnectionException;
import com.mz.sshclient.ssh.exceptions.SshDisconnectException;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ssh.sftp.SFtpConnector;
import com.mz.sshclient.ui.OpenedSshSessions;
import com.mz.sshclient.ui.actions.ActionCloseSshTab;
import com.mz.sshclient.ui.components.common.animation.ConnectAnimationComponent;
import com.mz.sshclient.ui.components.common.tabbedpane.CustomTabbedPaneClosable;
import com.mz.sshclient.ui.components.tabs.terminal.HostKeyVerificationCallback;
import com.mz.sshclient.ui.components.tabs.terminal.InteractiveResponseProvider;
import com.mz.sshclient.ui.components.tabs.terminal.PasswordRetryCallback;
import com.mz.sshclient.ui.components.tabs.terminal.PrivateKeyPasswordFinderCallback;
import com.mz.sshclient.ui.config.AppConfig;
import com.mz.sshclient.ui.utils.AWTInvokerUtils;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

public class TabContainerPanel extends JPanel implements ISshConnectionListener, IClosedSshConnectionCallback {

    private static final Logger LOG = LogManager.getLogger(TabContainerPanel.class);

    private static int counterSameTabName = 1;

    private final ISshConnectionObservableService sshConnectionService = ServiceRegistry.get(ISshConnectionObservableService.class);

    private CustomTabbedPaneClosable tabbedPane;

    public TabContainerPanel() {
        init();

        sshConnectionService.addConnectSshListener(this);
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY));

        tabbedPane = new CustomTabbedPaneClosable();

        /*for (int i = 1; i <= 30; i++) {
            tabbedPane.addTab("Shell-" + i, new TabContentPanel(null));
        }*/

        add(tabbedPane);
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

    private void connectSshClient(final SessionItemModel itemRef) {
        final ConnectAnimationComponent anime = new ConnectAnimationComponent(mzSimpleSshClientMain.MAIN_FRAME, "Connecting SSH ");
        anime.start();

        final SessionItemModel item = itemRef.copy();

        try {
            final SshClient sshClient = createSshClient(item);
            sshClient.connect();

            final SshTtyConnector sshTtyConnector = new SshTtyConnector(sshClient);
            sshTtyConnector.addClosedSshConnectionCallback(this);

            final SshClient sftpClient = createSshClient(item);
            final SFtpConnector sFtpConnector = new SFtpConnector(item, sftpClient);

            SwingUtilities.invokeLater(() -> {
                final TabContentPanel tabContainerPanel = new TabContentPanel(sshTtyConnector, sFtpConnector);

                final String tabName = OpenedSshSessions.hasSameTabName(item.getName()) ? item.getName() + " - " + (counterSameTabName++) : item.getName();

                tabbedPane.addTabWithAction(tabName, tabContainerPanel, new ActionCloseSshTab(sshTtyConnector));
                tabbedPane.toggleTabLayoutPolicy();

                final int index = tabbedPane.getTabCount() - 1;
                tabbedPane.setSelectedIndex(index);

                OpenedSshSessions.addSshSession(item, sshTtyConnector, sFtpConnector, index);

                anime.stop();
            });
        } catch (SshConnectionException | SshDisconnectException | SshOperationCanceledException e) {
            anime.stop();

            LOG.error("Could not connect", e);
            MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
        }
    }

    @Override
    public void connectSsh(final ConnectSshEvent event) {
        final SessionItemModel item = event.getSessionItemModel();
        AWTInvokerUtils.invokeExclusivelyNotInEventDispatcher(() -> connectSshClient(item));
    }

    @Override
    public void closedSshConnection(final Serializable reference) {
        Optional<OpenedSshSessions.SshSessionHolder> sshTerminalHolderOptional = OpenedSshSessions.getOpenSshSessions()
                .stream()
                .filter(ref -> ref.getSessionItemModel().getId() == ((SessionItemModel) reference).getId() &&
                        ref.getSessionItemModel() == reference)
                .findFirst();

        sshTerminalHolderOptional.ifPresent(item -> {
            tabbedPane.removeTabAt(item.getIndex());
            OpenedSshSessions.removeSshSession(item);
        });
    }

}
