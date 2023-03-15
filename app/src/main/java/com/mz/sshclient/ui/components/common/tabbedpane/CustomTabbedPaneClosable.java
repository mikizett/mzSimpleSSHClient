/**
 * Reference:
 *   https://github.com/jbw-software/JTabbedPaneExtended/blob/main/src/main/java/javax/swing/extended/JTabbedPaneExtended.java
 */
package com.mz.sshclient.ui.components.common.tabbedpane;

import com.mz.sshclient.ui.laf.metal.CustomMetalTabbedPaneUIDecorator;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CustomTabbedPaneClosable extends JTabbedPane {
    private CustomMetalTabbedPaneUIDecorator customMetalTabbedPaneUIDecorator;
    private PropertyChangeListener tabLayoutPolicyListener;

    private ClosableHeaderTabComponent closableHeaderTabComponent;

    private volatile boolean skipInvalidate = false;

    public CustomTabbedPaneClosable() {
        // Narrow the right gap added around tab component of L&F;
        // MetalLookAndFeel default insets are: (0, 9, 1, 9).
        UIManager.put("TabbedPane.tabInsets", new InsetsUIResource(0, 9, 1, 1));

        // Increase the gap between label and icon or button in SCROLL_TAB_LAYOUT
        UIManager.put("TabbedPane.textIconGap", 4);
    }

    private void installListeners() {
        if ((tabLayoutPolicyListener = this::tabLayoutPolicyChange) != null) {
            addPropertyChangeListener("tabLayoutPolicy", this::tabLayoutPolicyChange);
            setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }
    }

    private void uninstallListeners() {
        if (tabLayoutPolicyListener != null) {
            removePropertyChangeListener(tabLayoutPolicyListener);
            tabLayoutPolicyListener = null;
        }
    }

    private void tabLayoutPolicyChange(PropertyChangeEvent evt) {
        if ("tabLayoutPolicy".equals(evt.getPropertyName())) {
            if ((int) evt.getNewValue() == JTabbedPane.SCROLL_TAB_LAYOUT) {
                // Ensure that selected index is within visible scroll area
                final int selectedIndex = this.getSelectedIndex();
                if (selectedIndex >= 0) {
                    this.setSelectedIndex(selectedIndex);
                }
            }
        }
    }

    private void addCloseableTabComponent() {
        addCloseableTabComponent(null, null);
    }

    private void addCloseableTabComponent(String title, Action action) {
        final int countTabComponent = getTabCount();
        final int index = Math.max(countTabComponent - 1, 0);

        if (title == null) {
            title = getTitleAt(index);
        }

        closableHeaderTabComponent = new ClosableHeaderTabComponent(this, title, action);
        setTabComponentAt(index, closableHeaderTabComponent);
    }

    public void addTabWithAction(String title, Component component, Action action) {
        super.addTab(title, component);
        addCloseableTabComponent(title, action);
    }

    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
        super.addTab(title, icon, component, tip);
        addCloseableTabComponent(title, null);
    }

    @Override
    public void addTab(String title, Icon icon, Component component) {
        super.addTab(title, icon, component);
        addCloseableTabComponent(title, null);
    }

    @Override
    public void addTab(String title, Component component) {
        super.addTab(title, component);
        addCloseableTabComponent(title, null);
    }

    @Override
    public Component add(String title, Component component) {
        final Component comp = super.add(title, component);

        addCloseableTabComponent();

        return comp;
    }

    @Override
    public Component add(Component component, int index) {
        final Component comp = super.add(component, index);

        addCloseableTabComponent();

        return comp;
    }

    @Override
    public void add(Component component, Object constraints) {
        super.add(component, constraints);
        addCloseableTabComponent();
    }

    @Override
    public void add(Component component, Object constraints, int index) {
        super.add(component, constraints, index);
        addCloseableTabComponent();
    }

    public void setClosableHeaderTabAction(final Action action) {
        closableHeaderTabComponent.addCloseableHeaderAction(action);
    }

    @Override
    public void setUI(TabbedPaneUI ui) {
        if ("javax.swing.plaf.metal.MetalTabbedPaneUI".equals(ui.getClass().getName())) {
            if (this.customMetalTabbedPaneUIDecorator == null) {
                this.customMetalTabbedPaneUIDecorator = new CustomMetalTabbedPaneUIDecorator();
                ui = this.customMetalTabbedPaneUIDecorator;
            }
        }
        super.setUI(ui);
    }

    /**
     * Overrides setSelectedIndex in particular for the usage with scroll tab
     * layout to ensure that the selected index is scrolled into the visible
     * scroll range.
     *
     * @param index Index to be selected.
     */
    @Override
    public void setSelectedIndex(final int index) {
        if (index >= this.getTabCount() || index < 0) {
            return;
        }

        try {
            super.setSelectedIndex(index);
        } catch (final ArrayIndexOutOfBoundsException exception) {
            return;
        }

        // For JTabbedPane.SCROLL_TAB_LAYOUT, ensure that selected index is visible
        if (this.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            // Use non-native method scrollTabToVisible from AveBasicTabbedPaneUI
            ((CustomMetalTabbedPaneUIDecorator) this.getUI()).scrollTabToVisible(index);

            // Redo Layout to adapt to changes due to scrolling.
            this.doLayout();
        }
    }

    /**
     * There are lots of additional calls to invalidate because of the change of layouts in {@code CustomMetalTabbedPaneUIDecorator}'s runWithOriginalLayoutManager method.
     * To avoid unnecessary validation of TabbedPane's children these calls are skiped.
     */
    @Override
    public void invalidate() {
        if (!this.skipInvalidate) {
            super.invalidate();
        }
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

    public void setSkipNextInvalidate(boolean skipNextInvalidate) {
        if (this.skipInvalidate != skipNextInvalidate) {
            this.skipInvalidate = skipNextInvalidate;
        }
    }

    /**
     * Toggles the tab layout policy between {@code JTabbedPane.WRAP_TAB_LAYOUT}
     * and {@code JTabbedPane.SCROLL_TAB_LAYOUT}.
     */
    public void toggleTabLayoutPolicy() {
        if (this.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            this.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        } else {
            this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }
    }
}
