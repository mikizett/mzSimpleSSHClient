package com.mz.sshclient.ui.components.session.panels.add_or_edit;

import com.mz.sshclient.model.session.SessionItemDraftModel;
import com.mz.sshclient.ui.events.listener.IValueChangeListener;
import com.mz.sshclient.ui.events.listener.InputFieldDocumentListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Component;

public class SessionNamePanel extends JPanel implements IAdjustableSessionItemDraftPanel {
    private final SessionItemDraftModel sessionItemDraftModel;

    private final JTextField sessionNameTextField = new JTextField(10);

    private final IValueChangeListener valueChangeListener;

    public SessionNamePanel(
            final SessionItemDraftModel sessionItemDraftModel,
            final AddOrEditEnum addOrEditEnum,
            final IValueChangeListener valueChangeListener
    ) {
        this.sessionItemDraftModel = sessionItemDraftModel;
        this.valueChangeListener = valueChangeListener;

        init();
        if (addOrEditEnum == AddOrEditEnum.EDIT) {
            initData();
        }
        addListeners();
    }

    private void init() {
        BoxLayout sessionNameBoxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(sessionNameBoxLayout);
        setBorder(new EmptyBorder(10, 10, 0, 10));

        final JLabel sessionNameLabel = new JLabel("Session Name");
        sessionNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sessionNameLabel.setHorizontalAlignment(JLabel.LEADING);
        sessionNameLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        sessionNameTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        sessionNameTextField.setHorizontalAlignment(JLabel.LEADING);

        add(sessionNameLabel);
        add(sessionNameTextField);
    }

    private void initData() {
        sessionNameTextField.setText(sessionItemDraftModel.getName());
    }

    private void addListeners() {
        sessionNameTextField.getDocument().addDocumentListener(new InputFieldDocumentListener(valueChangeListener));
    }

    public String getSessionName() {
        return sessionNameTextField.getText();
    }

    public boolean hasValueChanged() {
        return !sessionNameTextField.getText().equals(sessionItemDraftModel.getName());
    }

    @Override
    public void adjustSessionItemDraft() {
        sessionItemDraftModel.setName(sessionNameTextField.getText());
    }
}
