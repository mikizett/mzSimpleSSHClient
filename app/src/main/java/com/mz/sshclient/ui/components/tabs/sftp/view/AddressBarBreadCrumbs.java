package com.mz.sshclient.ui.components.tabs.sftp.view;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AddressBarBreadCrumbs extends JPanel {

    private final boolean unix;
    private final MouseAdapter mouseAdapter;
    private String[] segments;
    private final List<ActionListener> listeners = new ArrayList<>(0);

    public AddressBarBreadCrumbs(boolean unix, ActionListener popupTriggerListener) {
        super(new AddressBarLayout());
        this.unix = unix;
        segments = new String[0];
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedPath = calculatePath((JComponent) e.getComponent());
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (popupTriggerListener != null) {
                        popupTriggerListener.actionPerformed(new ActionEvent(e, hashCode(), selectedPath));
                    }
                } else {
                    for (ActionListener l : listeners) {
                        l.actionPerformed(new ActionEvent(this, hashCode(), selectedPath));
                    }
                }
            }
        };
    }

    private String calculatePath(JComponent c) {
        for (int i = 0; i < this.getComponentCount(); i++) {
            JComponent cc = (JComponent) getComponent(i);
            if (c == cc) {
                Integer index = (Integer) cc.getClientProperty("path.index");
                if (index != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int k = 0; k <= index; k++) {
                        if (k != 0) {
                            stringBuilder.append(unix ? "/" : "\\");
                        }
                        stringBuilder.append(segments[k]);
                    }
                    return stringBuilder.toString();
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Dimension getPreferredSize() {
        return getLayout().preferredLayoutSize(this);
    }

    public void setPath(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }

        for (int i = 0; i < this.getComponentCount(); i++) {
            JComponent cc = (JComponent) getComponent(i);
            cc.removeMouseListener(mouseAdapter);
        }

        this.removeAll();
        segments = path.split(unix ? "\\/" : "\\\\");
        for (int i = 0; i < segments.length; i++) {
            String text = segments[i];
            if (text.length() < 1)
                continue;
            JButton btn = new JButton(segments[i]);
            btn.addMouseListener(mouseAdapter);
            btn.putClientProperty("path.index", i);
            if (i == segments.length - 1) {
                btn.putClientProperty("path.index.last", true);
            }
            add(btn);
        }

        this.doLayout();
        this.revalidate();
        this.repaint();
    }

    public void addActionListener(ActionListener a) {
        listeners.add(a);
    }

    public String getSelectedText() {
        return "";
    }

}
