package com.mz.sshclient.ui.events;

import javax.swing.event.TreeSelectionEvent;
import java.io.Serializable;
public class TreeSelectionNodeEvent implements Serializable {
    private final TreeSelectionEvent treeSelectionEvent;
    private final Object userObject;

    public TreeSelectionNodeEvent(final TreeSelectionEvent treeSelectionEvent, final Object userObject) {
        this.treeSelectionEvent = treeSelectionEvent;
        this.userObject = userObject;
    }

    public TreeSelectionEvent getTreeSelectionEvent() {
        return treeSelectionEvent;
    }

    public Object getUserObject() {
        return userObject;
    }
}
