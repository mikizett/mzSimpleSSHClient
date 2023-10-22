package com.mz.sshclient.services.events;

import com.mz.sshclient.model.session.SessionItemModel;

import java.io.Serializable;
import java.util.EventObject;

public class ConnectSshEvent extends EventObject implements Serializable {

    private final SessionItemModel sessionItemModel;

    public ConnectSshEvent(Object source) {
        this(source, null);
    }

    public ConnectSshEvent(Object source, final SessionItemModel sessionItemModel) {
        super(source);
        this.sessionItemModel = sessionItemModel;
    }

    public SessionItemModel getSessionItemModel() {
        return sessionItemModel;
    }
}
