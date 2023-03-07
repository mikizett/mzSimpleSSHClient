package com.mz.sshclient.services.events;

import com.mz.sshclient.model.SessionItemModel;

import java.io.Serializable;
import java.util.EventObject;

public class ConnectSSHEvent extends EventObject implements Serializable {

    private final SessionItemModel sessionItemModel;

    public ConnectSSHEvent(Object source) {
        this(source, null);
    }

    public ConnectSSHEvent(Object source, final SessionItemModel sessionItemModel) {
        super(source);
        this.sessionItemModel = sessionItemModel;
    }

    public SessionItemModel getSessionItemModel() {
        return sessionItemModel;
    }
}
