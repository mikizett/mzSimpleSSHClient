package com.mz.sshclient.ui.components.session.panels.add_or_edit;

import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionItemDraftModel;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.model.SessionItemModelHelper;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.exceptions.PasswordStorageException;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;
import com.mz.sshclient.services.interfaces.IPasswordStorageService;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.events.listener.IValueChangeListener;
import com.mz.sshclient.ui.utils.MasterPasswordUtil;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import com.mz.sshclient.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddOrEditSessionPanel extends JPanel implements IValueChangeListener {

    private static final Logger LOG = LogManager.getLogger(AddOrEditSessionPanel.class);

    private final Window parentWindow;
    private final JTree tree;

    private SessionItemDraftModel sessionItemDraftModel;
    private SessionItemModel sessionItemModel;

    private DefaultMutableTreeNode selectedTreeNode;
    private DefaultMutableTreeNode parentTreeNode;

    private AddOrEditEnum addOrEditEnum;

    private SessionNamePanel sessionNamePanel;
    private SessionInfoPanel sessionInfoPanel;

    private JButton connectButton;
    private JButton saveButton;
    private JButton cancelButton;

    private final IPasswordStorageService passwordStorageService = ServiceRegistry.get(IPasswordStorageService.class);
    private final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);

    private final List<IAdjustableSessionItemDraftPanel> adjustablePanels = new ArrayList<>(0);

    public AddOrEditSessionPanel(final Window parentWindow, final JTree tree) {
        this.parentWindow = parentWindow;
        this.tree = tree;
        final TreePath selectedPath = tree.getSelectionPath();
        if (selectedPath != null) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) (selectedPath.getLastPathComponent());
            if (selectedNode.getUserObject() instanceof SessionFolderModel) {
                parentTreeNode = selectedNode;
                setNewSessionItemDraftModel();
            } else {
                sessionItemModel = (SessionItemModel) selectedNode.getUserObject();
                sessionItemDraftModel = SessionItemModelHelper.convertToSessionItemDraftModel(sessionItemModel);
                selectedTreeNode = selectedNode;
                addOrEditEnum = AddOrEditEnum.EDIT;
            }
        } else {
            parentTreeNode = (DefaultMutableTreeNode) tree.getModel().getRoot();

            setNewSessionItemDraftModel();
        }
        init();
        initEnableComponents();
    }

    private void setNewSessionItemDraftModel() {
        sessionItemDraftModel = new SessionItemDraftModel();
        sessionItemDraftModel.setId(UUID.randomUUID().toString());
        addOrEditEnum = AddOrEditEnum.ADD;
    }

    private void init() {
        setLayout(new BorderLayout());

        final JPanel sessionNameAndSessionInfoPanel = new JPanel(new BorderLayout());

        sessionNamePanel = new SessionNamePanel(sessionItemDraftModel, addOrEditEnum, this);
        sessionNameAndSessionInfoPanel.add(sessionNamePanel, BorderLayout.NORTH);

        adjustablePanels.add(sessionNamePanel);

        sessionInfoPanel = new SessionInfoPanel(sessionItemDraftModel, addOrEditEnum, this);
        sessionNameAndSessionInfoPanel.add(sessionInfoPanel);

        adjustablePanels.add(sessionInfoPanel.getConnectionPanel());
        adjustablePanels.add(sessionInfoPanel.getSecureFtpPanel());
        adjustablePanels.add(sessionInfoPanel.getJumpHostPanel());

        final JScrollPane sessionNameAndSessionInfoScrollPane = new JScrollPane(sessionNameAndSessionInfoPanel);
        sessionNameAndSessionInfoScrollPane.setBorder(null);
        add(sessionNameAndSessionInfoScrollPane);

        connectButton = new JButton("Connect");
        connectButton.setEnabled(false);

        saveButton = new JButton("Save");
        saveButton.setEnabled(false);
        saveButton.addActionListener(l -> addSessionItem());

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(l -> parentWindow.dispose());

        final Box box = Box.createHorizontalBox();
        box.setBorder(new EmptyBorder(10, 5, 10, 10));

        final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(connectButton);
        box.add(p);

        box.add(Box.createHorizontalGlue());
        box.add(Box.createHorizontalStrut(10));
        box.add(saveButton);
        box.add(Box.createHorizontalStrut(10));
        box.add(cancelButton);

        add(box, BorderLayout.SOUTH);
    }

    private void initEnableComponents() {
        final boolean canEnableConnectButton = !sessionItemDraftModel.getName().isEmpty() &&
                !sessionItemDraftModel.getHost().isEmpty() && !sessionItemDraftModel.getPort().isEmpty();

        connectButton.setEnabled(canEnableConnectButton);
    }

    private void addSessionItem() {
        adjustablePanels.forEach(panel -> panel.adjustSessionItemDraft());

        if (addOrEditEnum == AddOrEditEnum.ADD) {
            sessionItemModel = SessionItemModelHelper.convertToSessionItemModel(sessionItemDraftModel);
            final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(sessionItemModel);
            childNode.setAllowsChildren(false);
            ((DefaultTreeModel) tree.getModel()).insertNodeInto(childNode, parentTreeNode, parentTreeNode.getChildCount());
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
            TreePath path = new TreePath(childNode.getPath());
            tree.setSelectionPath(path);
        } else {
            if (!sessionItemDraftModel.equals(sessionItemModel)) {
                sessionItemModel.deepCopyFrom(sessionItemDraftModel);

                ((DefaultTreeModel) tree.getModel()).reload();
                TreePath path = new TreePath(selectedTreeNode.getPath());
                tree.scrollPathToVisible(path);
                tree.setSelectionPath(path);
            }
        }

        saveSessionItem();

        parentWindow.dispose();
    }

    private void saveSessionItem() {
        if (StringUtils.isNotBlank(sessionItemModel.getPassword())) {
            final boolean passwordStorageFileExist = passwordStorageService.existStorageFile();
            final String message = passwordStorageFileExist
                    ? MasterPasswordUtil.getMessageToReadSessionsWithMasterPassword()
                    : MasterPasswordUtil.getMessageToStoreSessionsWithMasterPassword();

            final MasterPasswordUtil.MasterPasswordAnswer answer = MasterPasswordUtil.showMasterPasswordDialog(message);
            if (answer.getAnswerType() != JOptionPane.YES_OPTION) {
                MessageDisplayUtil.showMessage(mzSimpleSshClientMain.MAIN_FRAME, "The sessions won't be stored with passwords!");
            } else {
                if (!passwordStorageFileExist) {
                    char[] password = answer.getPassword();
                    final char[] passwordEncoded = Utils.encodeString(new String(password)).toCharArray();
                    try {
                        passwordStorageService.addMasterPassword(passwordEncoded);

                        // reset passwd
                        password = new char[] {'0'};
                    } catch (PasswordStorageException e) {
                        LOG.error(e);
                        MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
                    }
                }

                try {
                    passwordStorageService.storePassword(sessionItemModel);
                } catch (PasswordStorageException e) {
                    LOG.error(e);
                    MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
                }
            }

            try {
                sessionDataService.saveToFile();
            } catch (SaveSessionDataException e) {
                LOG.error(e);
                MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
            }
        }
    }

    @Override
    public void valueChanged() {
        final boolean canEnableButton = sessionNamePanel != null &&
                !sessionNamePanel.getSessionName().isEmpty() &&
                sessionInfoPanel != null && !sessionInfoPanel.getConnectionPanel().getHost().isEmpty() &&
                !sessionInfoPanel.getConnectionPanel().getPort().isEmpty() &&
                !sessionInfoPanel.getConnectionPanel().getUser().isEmpty();

        connectButton.setEnabled(canEnableButton);
        saveButton.setEnabled(canEnableButton);
    }
}
