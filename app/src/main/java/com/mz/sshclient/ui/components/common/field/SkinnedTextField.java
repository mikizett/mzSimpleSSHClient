package com.mz.sshclient.ui.components.common.field;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SkinnedTextField extends JTextField {

    public SkinnedTextField() {
        super();
        installPopUp();
    }

    public SkinnedTextField(int cols) {
        super(cols);
        installPopUp();
        //getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), InputEvent.SE);
    }

    private void installPopUp() {
        this.putClientProperty("flat.popup", createPopup());
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {

                    JPopupMenu pop = (JPopupMenu) SkinnedTextField.this.getClientProperty("flat.popup");
                    if (pop != null) {
                        pop.show(SkinnedTextField.this, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private JPopupMenu createPopup() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem mCut = new JMenuItem("Cut");
        JMenuItem mCopy = new JMenuItem("Copy");
        JMenuItem mPaste = new JMenuItem("Paste");
        JMenuItem mSelect = new JMenuItem("Select All");

        popup.add(mCut);
        popup.add(mCopy);
        popup.add(mPaste);
        popup.add(mSelect);

        mCut.addActionListener(e -> {
            cut();
        });

        mCopy.addActionListener(e -> {
            copy();
        });

        mPaste.addActionListener(e -> {
            paste();
        });

        mSelect.addActionListener(e -> {
            selectAll();
        });

        return popup;
    }

}
