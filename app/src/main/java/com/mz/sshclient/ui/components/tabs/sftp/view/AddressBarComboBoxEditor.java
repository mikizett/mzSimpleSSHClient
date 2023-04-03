package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ui.components.common.field.SkinnedTextField;

import javax.swing.ComboBoxEditor;
import java.awt.Component;

public class AddressBarComboBoxEditor extends SkinnedTextField implements ComboBoxEditor {

    public AddressBarComboBoxEditor() {
        super.putClientProperty("paintNoBorder", "True");
    }

    @Override
    public Component getEditorComponent() {
        return this;
    }

    @Override
    public Object getItem() {
        return getText();
    }

    @Override
    public void setItem(Object anObject) {
        if (anObject != null) {
            setText(anObject.toString());
        }
    }

}
