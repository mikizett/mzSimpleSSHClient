package com.mz.sshclient.ui.components.common.tree;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class TreeTransferHandler extends TransferHandler {

    private static final Logger LOG = LogManager.getLogger(TreeTransferHandler.class);

    private DataFlavor nodesFlavor;
    private DataFlavor[] flavors = new DataFlavor[1];
    private DefaultMutableTreeNode toRemove;

    public TreeTransferHandler() {
        try {
            final String mimeType =
                    DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" +
                            DefaultMutableTreeNode[].class.getName() + "\"";

            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch (ClassNotFoundException e) {
            LOG.error("ClassNotFound: " + e.getMessage());
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if (!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        // Do not allow a drop on the drag source selections
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        JTree tree = (JTree) support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for (int i = 0; i < selRows.length; i++) {
            if (selRows[i] == dropRow) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null && paths.length > 0) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();

            toRemove = node;
            return new NodesTransferable(node);
        }
        return null;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {}

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        DefaultMutableTreeNode node = null;
        try {
            Transferable t = support.getTransferable();
            node = (DefaultMutableTreeNode) t.getTransferData(nodesFlavor);
        } catch (UnsupportedFlavorException e) {
            LOG.error("UnsupportedFlavor: " + e);
        } catch (java.io.IOException ioe) {
            LOG.error("I/O error: " + ioe);
        }
        // Get drop location info.
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
        JTree tree = (JTree) support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.removeNodeFromParent(node);
        // Configure for drop mode.
        int index = childIndex; // DropMode.INSERT
        if (childIndex == -1) { // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
        model.insertNodeInto(node, parent, index++);
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

    public class NodesTransferable implements Transferable {
        DefaultMutableTreeNode node;

        public NodesTransferable(DefaultMutableTreeNode node) {
            this.node = node;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return node;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    };

}
