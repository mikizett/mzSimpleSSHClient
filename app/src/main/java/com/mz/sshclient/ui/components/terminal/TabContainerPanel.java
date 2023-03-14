package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.Constants;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.events.ConnectSshEvent;
import com.mz.sshclient.services.events.listener.ISshConnectListener;
import com.mz.sshclient.services.interfaces.ISSHConnectionObservableService;
import com.mz.sshclient.ssh.HostKeyVerifier;
import com.mz.sshclient.ssh.SshClient;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ssh.exceptions.SshConnectionException;
import com.mz.sshclient.ssh.exceptions.SshDisconnectException;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ui.components.common.tabbedpane.CustomTabbedPaneClosable;
import com.mz.sshclient.ui.config.AppConfig;
import com.mz.sshclient.ui.utils.AWTInvokerUtils;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

public class TabContainerPanel extends JPanel implements ISshConnectListener {

    private static final Logger LOG = LogManager.getLogger(TabContainerPanel.class);

    private final ISSHConnectionObservableService sshConnectionService = ServiceRegistry.get(ISSHConnectionObservableService.class);

    private CustomTabbedPaneClosable tabbedPane;

    private HostKeyVerifier hostKeyVerifier;

    private SshClient sshClient;

    private final PasswordFinderCallback passwordFinderCallback = new PasswordFinderCallback();
    private final InteractiveResponseProvider interactiveResponseProvider = new InteractiveResponseProvider();
    private final PasswordRetryCallback passwordRetryCallback = new PasswordRetryCallback();

    public TabContainerPanel() {
        init();
        initHostKeyVerifier();

        sshConnectionService.addConnectSshListener(this);
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY));

        tabbedPane = new CustomTabbedPaneClosable();

        /*for (int i = 1; i <= 30; i++) {
            tabbedPane.addTab("Shell-" + i, new TabContentPanel());
        }*/

        add(tabbedPane);
    }

    private void initHostKeyVerifier() {
        try {
            final File hostKeyFile = new File(AppConfig.getKnownHostsLocation());
            hostKeyVerifier  = new HostKeyVerifier(hostKeyFile, new HostKeyVerificationCallback());
        } catch (IOException e) {
            LOG.error("Could not load host key verifier <" + Constants.KNOWN_HOSTS_FILE_NAME + ">" , e);
        }
    }

    private void createSshClient(final SessionItemModel item) {
        sshClient = new SshClient(
                item,
                hostKeyVerifier,
                passwordFinderCallback,
                interactiveResponseProvider,
                passwordRetryCallback
        );
    }

    private void connectSshClient(final SessionItemModel item) {
        try {
            sshClient.connect();

            final SshTtyConnector sshTtyConnector = new SshTtyConnector(sshClient);

            tabbedPane.addTab(item.getName(), new TabContentPanel(sshTtyConnector));
            tabbedPane.toggleTabLayoutPolicy();

        } catch (SshConnectionException | SshDisconnectException | SshOperationCanceledException e) {
            LOG.error("Could not connect", e);
            MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
        }
    }

    @Override
    public void connectSsh(final ConnectSshEvent event) {
        final SessionItemModel item = event.getSessionItemModel();
        AWTInvokerUtils.invokeExclusivelyNotInEventDispatcher(() -> {
            createSshClient(item);
            connectSshClient(item);
        });

        //tabbedPane.addTab(item.getName(), new TabContentPanel());
        //tabbedPane.toggleTabLayoutPolicy();
    }

}
