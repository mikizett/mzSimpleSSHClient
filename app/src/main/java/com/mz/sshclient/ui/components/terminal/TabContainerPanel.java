package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.Constants;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.events.ConnectSshEvent;
import com.mz.sshclient.services.events.listener.ISshConnectionListener;
import com.mz.sshclient.services.interfaces.ISSHConnectionObservableService;
import com.mz.sshclient.ssh.HostKeyVerifier;
import com.mz.sshclient.ssh.IClosedSshConnectionCallback;
import com.mz.sshclient.ssh.SshClient;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ssh.exceptions.SshConnectionException;
import com.mz.sshclient.ssh.exceptions.SshDisconnectException;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ui.OpenedSshSessions;
import com.mz.sshclient.ui.actions.ActionCloseSshTab;
import com.mz.sshclient.ui.components.common.tabbedpane.CustomTabbedPaneClosable;
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
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class TabContainerPanel extends JPanel implements ISshConnectionListener, IClosedSshConnectionCallback {

    private static final Logger LOG = LogManager.getLogger(TabContainerPanel.class);

    private final ISSHConnectionObservableService sshConnectionService = ServiceRegistry.get(ISSHConnectionObservableService.class);

    private CustomTabbedPaneClosable tabbedPane;

    private SshClient sshClient;

    //private final List<SshTerminalHolder> sshTerminalList = new ArrayList<>(0);

    private HostKeyVerifier hostKeyVerifier;
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
            sshTtyConnector.addClosedSshConnectionCallback(this);

            SwingUtilities.invokeLater(() -> {
                final TabContentPanel tabContainerPanel = new TabContentPanel(sshTtyConnector);
                tabbedPane.addTabWithAction(item.getName(), tabContainerPanel, new ActionCloseSshTab(sshTtyConnector));
                tabbedPane.toggleTabLayoutPolicy();
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

                OpenedSshSessions.addSshSession(tabContainerPanel, item, sshTtyConnector);
            });
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
    }

    @Override
    public void closedSshConnection(final Serializable reference) {
        Optional<OpenedSshSessions.SshSessionHolder> sshTerminalHolderOptional = OpenedSshSessions.getOpenSshSessions()
                .stream()
                .filter(ref -> ref.getSessionItemModel().getId() == ((SessionItemModel) reference).getId())
                .findFirst();

        sshTerminalHolderOptional.ifPresent(item -> {
            final int index = tabbedPane.indexOfComponent(item.getTabContentPanel());
            if (index > -1) {
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        tabbedPane.removeTabAt(index);
                        OpenedSshSessions.removeSshSession(item);
                    });
                } catch (Exception e) {
                    // do nothing !!!
                }
            }
        });
    }

}
