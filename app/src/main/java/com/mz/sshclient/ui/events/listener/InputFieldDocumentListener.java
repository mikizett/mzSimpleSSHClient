package com.mz.sshclient.ui.events.listener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class InputFieldDocumentListener implements DocumentListener {

    private final IValueChangeListener valueChangeListener;

    public InputFieldDocumentListener(final IValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        fireEvent();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fireEvent();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fireEvent();
    }

    private void fireEvent() {
        if (valueChangeListener != null) {
            valueChangeListener.valueChanged();
        }
    }
}
