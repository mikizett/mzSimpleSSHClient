package com.mz.sshclient.ui.events.listener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class InputFieldDocumentListener implements DocumentListener, ItemListener {

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

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            fireEvent();
        }
    }
}
