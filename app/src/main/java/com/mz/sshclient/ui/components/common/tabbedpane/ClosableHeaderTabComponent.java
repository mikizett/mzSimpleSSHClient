/**
 * Reference:
 *   https://github.com/jbw-software/JTabbedPaneExtended/blob/main/src/main/java/javax/swing/extended/ClosableTabComponent.java
 */
package com.mz.sshclient.ui.components.common.tabbedpane;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

class ClosableHeaderTabComponent extends JPanel implements IClosableHeaderTabComponent {
    private static final Color DEFAULT_BORDER_COLOR = Color.GRAY;
    private static final Color DEFAULT_CROSS_COLOR = Color.GRAY;
    private static final Color DEFAULT_CROSS_ROLL_OVER_COLOR = Color.RED;
    private static final int DEFAULT_TEXT_BUTTON_GAP = (int) UIManager.get("TabbedPane.textIconGap");
    private static final int TAB_BUTTON_SIZE = 15;
    private PropertyChangeListener tabLayoutPolicyListener;
    private final JTabbedPane tabbedPane;
    private final JLabel titleLabel;
    private final CloseTabButton closeTabButton;
    private final JPanel titlePanel;
    private final Color borderColor;
    private final Color crossColor;
    private final Color crossRolloverColor;
    private final int textButtonGap;

    protected ClosableHeaderTabComponent(final JTabbedPane tabbedPane, final String title) {
        this(tabbedPane, title, null);
    }

    protected ClosableHeaderTabComponent(final JTabbedPane tabbedPane, final String title, final Action action) {
        this(tabbedPane, title, action, DEFAULT_BORDER_COLOR, DEFAULT_CROSS_COLOR, DEFAULT_CROSS_ROLL_OVER_COLOR, DEFAULT_TEXT_BUTTON_GAP);
    }

    protected ClosableHeaderTabComponent(
            final JTabbedPane tabbedPane,
            final String title,
            final Action action,
            final Color borderColor,
            final Color crossColor,
            final Color crossRolloverColor,
            int textButtonGap
    ) {
        if (tabbedPane == null) {
            throw new NullPointerException("tabbedPane is null.");
        }

        this.tabbedPane = tabbedPane;
        this.borderColor = borderColor;
        this.crossColor = crossColor;
        this.crossRolloverColor = crossRolloverColor;
        this.textButtonGap = textButtonGap;

        setLayout(new BorderLayout());
        setOpaque(false);

        setName(title);

        titleLabel = new JLabel(title);
        closeTabButton = action != null ? new CloseTabButton(this, action) : new CloseTabButton(this);

        titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.CENTER);

        // dirty hack to increase size
        titlePanel.setPreferredSize(new Dimension(titlePanel.getPreferredSize().width + 20, titlePanel.getPreferredSize().height));

        final JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        closeButtonPanel.setOpaque(false);
        closeButtonPanel.add(closeTabButton);
        add(closeButtonPanel, BorderLayout.LINE_END);

        installListeners();
    }

    private void installListeners() {
        if ((tabLayoutPolicyListener = this::tabLayoutPolicyChange) != null) {
            tabbedPane.addPropertyChangeListener("tabLayoutPolicy", this::tabLayoutPolicyChange);
        }
    }

    private void uninstallListeners() {
        if (tabLayoutPolicyListener != null) {
            tabbedPane.removePropertyChangeListener(tabLayoutPolicyListener);
            tabLayoutPolicyListener = null;
        }
    }

    private final static MouseListener BUTTON_MOUSE_LISTENER = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    private void tabLayoutPolicyChange(PropertyChangeEvent evt) {
        if ("tabLayoutPolicy".equals(evt.getPropertyName())) {
            if ((int) evt.getNewValue() == JTabbedPane.SCROLL_TAB_LAYOUT) {
                // Add a gap between the label and the button.
                titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, this.textButtonGap));
            } else {
                titlePanel.setBorder(null);
            }
        }
    }

    @Override
    public void addCloseableHeaderAction(Action action) {
        closeTabButton.setAction(action);
    }

    @Override
    public void addNotify() {
        this.installListeners();
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        this.uninstallListeners();
    }

    /**
     * The CloseTabButton class should not be instantiated externally, but since the
     * button's action is accessable externaly, the class is made "puplic" to
     * allow casting of the event's source to this class.
     */
    private final class CloseTabButton extends JButton {
        private final AbstractAction DEFAULT_CLOSE_ACTION = new RemoveTabAction();
        private final Action action;
        private final ClosableHeaderTabComponent tabComponent;

        private CloseTabButton(final ClosableHeaderTabComponent tabComponent) {
            this(tabComponent, null);
        }

        private CloseTabButton(final ClosableHeaderTabComponent tabComponent, final Action action) {
            this.tabComponent = tabComponent;

            this.action = action;
            setPreferredSize(new Dimension(TAB_BUTTON_SIZE, TAB_BUTTON_SIZE));

            // Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            // Make it transparent
            setContentAreaFilled(false);
            // No need to be focusable.
            setFocusable(false);
            // Make a simple border
            setBorder(BorderFactory.createLineBorder(borderColor));
            setBorderPainted(false);
            // Making nice rollover effect
            setRolloverEnabled(true);
        }

        private void installListeners() {
            // Use the same listener for all buttons.
            addMouseListener(BUTTON_MOUSE_LISTENER);
            if (action != null) {
                setAction(action);
            }
            // Overwrite Mnemonic after setting the action.
            setMnemonic(0);
            setText("");
        }

        private void removeListener() {
            removeMouseListener(BUTTON_MOUSE_LISTENER);
            setAction(null);
        }

        @Override
        protected void fireActionPerformed(ActionEvent event) {
            super.fireActionPerformed(event);
            if (action == null) {
                DEFAULT_CLOSE_ACTION.actionPerformed(event);
            }
        }

        /**
         * Notifies this component that it no longer has a parent component.
         * This method is called by the toolkit internally and should not be
         * called directly by programs.
         */
        @Override
        public void removeNotify() {
            removeListener();
            super.removeNotify();
        }

        /**
         * Notifies this component that it now has a parent component. This
         * method is called by the toolkit internally and should not be called
         * directly by programs.
         */
        @Override
        public void addNotify() {
            installListeners();
            super.addNotify();
        }

        // Do not anything for this button if the UI wants to update.
        @Override
        public void updateUI() {
        }

        // Paint the "closing cross"
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // Shift the image for pressed buttons.
            if (getModel().isPressed()) {
                g2.translate(0.5f, 0.5f);
            }
            // Define the stroke to draw the cross
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g2.setColor(crossColor);
            if (getModel().isRollover()) {
                g2.setColor(crossRolloverColor);
            }
            int gap = 4; // The gap to all edges.
            g2.drawLine(gap, gap, getWidth() - gap - 1, getHeight() - gap - 1);
            g2.drawLine(getWidth() - gap - 1, gap, gap, getHeight() - gap - 1);
            g2.dispose();
        }
    };

    /**
     * Removes a tab by clicking on the close button
     */
    public class RemoveTabAction extends AbstractAction {
        private final static String SHORT_DESCRIPTION_CLOSE = "Close selected tab";

        protected RemoveTabAction() {
            super("Close");
            putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_CLOSE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = indexOfTabComponent(tabbedPane, ClosableHeaderTabComponent.this);
            if (index > -1 && tabbedPane.getTabCount() > 0) {
                tabbedPane.removeTabAt(index);
                tabbedPane.revalidate();
                tabbedPane.repaint();
            }
        }

        private int indexOfTabComponent(final JTabbedPane tabbedPane, final Component tabComponent) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component c = tabbedPane.getTabComponentAt(i);
                if (c == tabComponent) {
                    return i;
                }
            }
            return -1;
        }
    };
}
