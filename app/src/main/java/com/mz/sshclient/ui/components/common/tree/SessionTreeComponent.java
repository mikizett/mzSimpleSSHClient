package com.mz.sshclient.ui.components.common.tree;

import com.mz.sshclient.model.AbstractSessionEntryModel;
import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.events.ConnectSshEvent;
import com.mz.sshclient.services.interfaces.ISSHConnectionObservableService;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.actions.ActionRenameSelectedTreeItem;
import com.mz.sshclient.ui.components.session.popup.SessionActionsPopupMenu;
import com.mz.sshclient.ui.events.listener.ISessionDataChangedListener;
import com.mz.sshclient.ui.events.listener.ITreeSelectionNodeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SessionTreeComponent extends JTree implements TreeSelectionListener, TreeModelListener {
    private static final Logger LOG = LogManager.getLogger(SessionTreeComponent.class);

    private final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);
    private final ISSHConnectionObservableService sshConnectionService = ServiceRegistry.get(ISSHConnectionObservableService.class);

    private DefaultTreeModel defaultTreeModel;
    private DefaultMutableTreeNode rootNode;

    private final SessionFolderModel sessionFolderModel;

    private AbstractSessionEntryModel selectedNodeModel;

    private final List<ISessionDataChangedListener> sessionDataChangedListeners = new ArrayList<>(0);
    private final List<ITreeSelectionNodeListener> treeSelectionNodeListeners = new ArrayList<>(0);

    public SessionTreeComponent() {
        this.sessionFolderModel = sessionDataService.getSessionModel().getFolder();
        init();
    }

    private void init() {
        rootNode = createTreeNodes(sessionFolderModel);
        rootNode.setAllowsChildren(true);
        defaultTreeModel = new DefaultTreeModel(rootNode, true);

        setModel(defaultTreeModel);

        setEditable(false);
        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
        setTransferHandler(new TreeTransferHandler());
        setInvokesStopCellEditing(true);

        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        getSelectionModel().addTreeSelectionListener(this);
        getModel().addTreeModelListener(this);

        getActionMap().put(getInputMap().get(KeyStroke.getKeyStroke("F2")), new ActionRenameSelectedTreeItem(this));
        addMouseListener(new TreeMouseListener());
    }

    private DefaultMutableTreeNode createTreeNodes(final SessionFolderModel sessionFolderModel) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(sessionFolderModel);

        sessionFolderModel.getItems().forEach(item -> {
            DefaultMutableTreeNode nodeItem = new DefaultMutableTreeNode(item);
            nodeItem.setAllowsChildren(false);
            node.add(nodeItem);
        });

        sessionFolderModel.getFolders().forEach(folder -> node.add(createTreeNodes(folder)));

        return node;
    }

    private DefaultMutableTreeNode getParentNode() {
        DefaultMutableTreeNode parentNode = null;

        final TreePath parentPath = getSelectionPath();
        if (parentPath != null) {
            parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        }
        return parentNode;
    }

    private boolean selectNode(String id, DefaultMutableTreeNode node) {
        final AbstractSessionEntryModel model = (AbstractSessionEntryModel) node.getUserObject();
        if (id.equals(model.getId())) {
            TreePath path = new TreePath(node.getPath());
            setSelectionPath(path);
            scrollPathToVisible(path);
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (selectNode(id, child)) {
                return true;
            }
        }
        return false;
    }

    private void fireSessionDataChangedEvent() {
        // in case data were not changed don't emit the event
        if (sessionDataService.hasSessionModelChanged()) {
            sessionDataChangedListeners.forEach(l -> l.sessionDataChanged());
        }
    }

    public void addNewSessionFolder() {
        setEditable(true);

        DefaultMutableTreeNode parentNode = getParentNode();
        if (parentNode == null) {
            parentNode = rootNode;
        }

        if (parentNode.getUserObject() instanceof SessionItemModel) {
            parentNode = (DefaultMutableTreeNode) parentNode.getParent();
        }

        final SessionFolderModel newSessionFolderModel = sessionDataService.createNewSessionFolder((SessionFolderModel) parentNode.getUserObject());

        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(newSessionFolderModel);

        defaultTreeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());

        final TreePath treePath = new TreePath(childNode.getPath());

        startEditingAtPath(treePath);

        scrollPathToVisible(new TreePath(childNode.getPath()));
        setSelectionPath(new TreePath(childNode.getPath()));
    }

    public void renameSelectedNode() {
        setEditable(true);

        TreePath selectedPath = getSelectionPath();
        startEditingAtPath(selectedPath);
    }

    public void deleteNode() {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
        final DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) node.getParent();
        if (node != null && parentTreeNode != null) {
            DefaultMutableTreeNode sibling = node.getNextSibling();
            if (sibling != null) {
                Object siblingUserObject = sibling.getUserObject();
                String id = ((AbstractSessionEntryModel) siblingUserObject).getId();
                selectNode(id, sibling);
            } else {
                setSelectionPath(new TreePath(parentTreeNode.getPath()));

                final Object nodeUserObject = node.getUserObject();

                // remove session item from selected parent folder
                final SessionFolderModel parentSessionFolderModel = (SessionFolderModel) parentTreeNode.getUserObject();

                if (nodeUserObject instanceof SessionItemModel) {
                    final SessionItemModel sessionItemModel = (SessionItemModel) node.getUserObject();
                    sessionDataService.removeSessionItemFrom(parentSessionFolderModel, sessionItemModel);
                }

                if (nodeUserObject instanceof SessionFolderModel) {
                    final SessionFolderModel sessionFolderModel = (SessionFolderModel) nodeUserObject;
                    sessionDataService.removeSessionFolderFrom(parentSessionFolderModel, sessionFolderModel);
                }

            }
            defaultTreeModel.removeNodeFromParent(node);
        }
    }

    public void cloneSelectedNode() {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
        final Object nodeUserObject = node.getUserObject();
        final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();

        if (node != null && parentNode != null && (nodeUserObject instanceof SessionItemModel)) {
            final SessionItemModel newSessionItem = ((SessionItemModel) node.getUserObject()).deepCopy();
            //newSessionItem.setName(newSessionItem.getName() + " (Copy)");

            final DefaultMutableTreeNode child = new DefaultMutableTreeNode(newSessionItem);
            child.setAllowsChildren(false);

            defaultTreeModel.insertNodeInto(child, (MutableTreeNode) node.getParent(), node.getParent().getChildCount());

            selectNode(newSessionItem.getId(), child);

            final SessionFolderModel parentSessionFolder = (SessionFolderModel) parentNode.getUserObject();
            sessionDataService.addSessionItem(parentSessionFolder, newSessionItem);

        } else if (node != null && parentNode != null && (nodeUserObject instanceof SessionFolderModel)) {
            final SessionFolderModel newSessionFolder = ((SessionFolderModel) node.getUserObject()).deepCopy();
            //newSessionFolder.setId(UUID.randomUUID().toString());
            //newSessionFolder.setName(((AbstractSessionEntryModel) nodeUserObject).getName() + " (Copy)");

            final DefaultMutableTreeNode newFolderTree = new DefaultMutableTreeNode(newSessionFolder);

            final SessionFolderModel parentSessionFolder = (SessionFolderModel) parentNode.getUserObject();
            sessionDataService.addSessionFolder(parentSessionFolder, newSessionFolder);

            final Enumeration<TreeNode> children = node.children();
            while (children.hasMoreElements()) {
                final DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) children.nextElement();
                final Object defaultMutalbeTreeNodeUserObject = defaultMutableTreeNode.getUserObject();
                if (defaultMutalbeTreeNodeUserObject instanceof SessionItemModel) {
                    final SessionItemModel newCopySessionItem = ((SessionItemModel) defaultMutableTreeNode.getUserObject()).deepCopy();
                    //newCopySessionItem.setName(newCopySessionItem.getName() + " (Copy)");

                    DefaultMutableTreeNode subChild = new DefaultMutableTreeNode(newCopySessionItem);
                    subChild.setAllowsChildren(false);

                    newFolderTree.add(subChild);

                    sessionDataService.addSessionItem(newSessionFolder, newCopySessionItem);

                } else if (defaultMutalbeTreeNodeUserObject instanceof SessionFolderModel) {
                    final SessionFolderModel copySessionFolder = ((SessionFolderModel) defaultMutalbeTreeNodeUserObject).deepCopy();
                    //newSessionFolder.setName(copySessionFolder.getName() + " (Copy)");

                    newFolderTree.add(cloneChildFolder(copySessionFolder));

                    sessionDataService.addSessionFolder(newSessionFolder, copySessionFolder);
                }
            }

            final MutableTreeNode parent = (MutableTreeNode) node.getParent();
            defaultTreeModel.insertNodeInto(newFolderTree, parent, node.getParent().getChildCount());

            selectNode(newSessionFolder.getId(), newFolderTree);
        }
    }

    private DefaultMutableTreeNode cloneChildFolder(final SessionFolderModel sessionFolderModel) {
        /*
        private DefaultMutableTreeNode createTreeNodes(final SessionFolderModel sessionFolderModel) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(sessionFolderModel);

            sessionFolderModel.getItems().forEach(item -> {
                DefaultMutableTreeNode nodeItem = new DefaultMutableTreeNode(item);
                nodeItem.setAllowsChildren(false);
                node.add(nodeItem);
            });

            sessionFolderModel.getFolders().forEach(folder -> node.add(createTreeNodes(folder)));

            return node;
        }
        */
        return createTreeNodes(sessionFolderModel);

    }

    public void addSessionDataChangedListener(final ISessionDataChangedListener l) {
        if (!sessionDataChangedListeners.contains(l)) {
            sessionDataChangedListeners.add(l);
        }
    }

    public void connectSsh() {
        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();
        if (selectedNode.getUserObject() instanceof SessionItemModel) {
            final SessionItemModel selectedSessionItemModel = (SessionItemModel) selectedNode.getUserObject();
            sshConnectionService.fireConnectSSHEvent(new ConnectSshEvent(this, selectedSessionItemModel));
        }
    }

    // -------------------------------------------------------------------------
    // TreeSelectionListener

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();
        if (selectedNode != null) {
            selectedNodeModel = (AbstractSessionEntryModel) selectedNode.getUserObject();
        }
    }

    // -------------------------------------------------------------------------
    // TreeModelListener

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        final DefaultMutableTreeNode parentTreeNode  = (DefaultMutableTreeNode) e.getTreePath().getLastPathComponent();

        final int indexOf = e.getChildIndices()[0];
        final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parentTreeNode.getChildAt(indexOf);
        if (childNode != null) {
            final String changedNodeName = (String) childNode.getUserObject();
            selectedNodeModel.setName(changedNodeName);
            childNode.setUserObject(selectedNodeModel);

            fireSessionDataChangedEvent();

            LOG.debug("Renamed tree path: " + changedNodeName + " for: " + parentTreeNode);
        }
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        fireSessionDataChangedEvent();

        LOG.debug("Inserted tree path: " + e.getTreePath());

        SessionFolderModel parentFolder = null;

        DefaultMutableTreeNode lastSessionFolderNode = (DefaultMutableTreeNode) e.getTreePath().getLastPathComponent();
        if (lastSessionFolderNode != null && lastSessionFolderNode.getUserObject() instanceof SessionFolderModel) {
            parentFolder = (SessionFolderModel) lastSessionFolderNode.getUserObject();
        }

        int[] index = e.getChildIndices();
        Object lastInsertedNode = lastSessionFolderNode.getChildAt(index[0]);
        if (lastInsertedNode != null && lastInsertedNode instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode insertedChild = (DefaultMutableTreeNode) lastInsertedNode;
            Object userObject = insertedChild.getUserObject();

            if (userObject instanceof SessionFolderModel) {
                final SessionFolderModel folder = (SessionFolderModel) userObject;
                sessionDataService.addSessionFolder(parentFolder, folder);
                LOG.debug("Inserted session folder: " + folder.getName() + " to session parentFolder: " + parentFolder.getName());

            } else if (userObject instanceof SessionItemModel) {
                final SessionItemModel item = (SessionItemModel) userObject;
                sessionDataService.addSessionItem(parentFolder, item);
                LOG.debug("Inserted session item: " + item.getName() + " to session parentFolder: " + parentFolder.getName());
            }
        }
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        fireSessionDataChangedEvent();
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        fireSessionDataChangedEvent();
    }

    /**
     * Handles mouse clicks on the tree component
     */
    private static final class TreeMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                final SessionTreeComponent tree = (SessionTreeComponent) e.getSource();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null || node.getAllowsChildren()) {
                    final TreePath treePath = new TreePath(node.getPath());
                    boolean isExpanded = !tree.isExpanded(treePath);
                    if (isExpanded) {
                        tree.collapsePath(treePath);
                    } else {
                        tree.expandPath(new TreePath(node.getPath()));
                    }
                } else {
                    tree.connectSsh();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
                showPopupMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger())
                showPopupMenu(e);
        }

        private void showPopupMenu(MouseEvent e) {
            final SessionTreeComponent tree = (SessionTreeComponent) e.getSource();
            if (SwingUtilities.isRightMouseButton(e)) {
                int row = tree.getRowForLocation(e.getX(), e.getY());
                if(row != -1) {
                    tree.setSelectionRow(row);
                    final SessionActionsPopupMenu popupMenu = new SessionActionsPopupMenu(tree);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    };

}
