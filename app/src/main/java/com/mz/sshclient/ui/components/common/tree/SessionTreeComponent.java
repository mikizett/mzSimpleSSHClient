package com.mz.sshclient.ui.components.common.tree;

import com.mz.sshclient.model.AbstractSessionEntryModel;
import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.services.ServiceRegistry;
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
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SessionTreeComponent extends JTree implements TreeSelectionListener, TreeModelListener {
    private static final Logger LOG = LogManager.getLogger(SessionTreeComponent.class);

    private final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);

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

        final SessionFolderModel newSessionFolderModel = sessionDataService.addSessionFolderModel((SessionFolderModel) parentNode.getUserObject());

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

    public void addSessionDataChangedListener(final ISessionDataChangedListener l) {
        if (!sessionDataChangedListeners.contains(l)) {
            sessionDataChangedListeners.add(l);
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
        System.out.println("******** treeNodesInserted");
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        System.out.println("******** treeNodesRemoved");
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        System.out.println("******** treeStructureChanged");
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
                    return;
                }
                // TODO: implement connection here
                //connectSSH();
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
