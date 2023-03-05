package com.mz.sshclient.ui.components.common.tree;

import com.mz.sshclient.ui.events.listener.ITreeSelectionNodeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;
import java.util.List;

public class SessionTreeComponent extends JTree implements TreeSelectionListener, TreeModelListener {
    private static final Logger LOG = LogManager.getLogger(SessionTreeComponent.class);

    private DefaultTreeModel defaultTreeModel;
    private DefaultMutableTreeNode rootNode;

    private final List<ITreeSelectionNodeListener> treeSelectionNodeListenerList = new ArrayList<>(0);

    public SessionTreeComponent() {
        init();
    }

    private void init() {
        rootNode = new DefaultMutableTreeNode("Sessions");
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
    }

    // -------------------------------------------------------------------------
    // TreeSelectionListener

    @Override
    public void valueChanged(TreeSelectionEvent e) {

    }

    // -------------------------------------------------------------------------
    // TreeModelListener

    @Override
    public void treeNodesChanged(TreeModelEvent e) {

    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {

    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        System.out.println("--- REMOVED");
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        System.out.println("--- CHANGED");
    }

}
