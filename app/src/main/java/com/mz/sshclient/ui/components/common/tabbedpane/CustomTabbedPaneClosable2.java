/**
 * Reference:
 *   https://github.com/jbw-software/JTabbedPaneExtended/blob/main/src/main/java/javax/swing/extended/JTabbedPaneExtended.java
 */
package com.mz.sshclient.ui.components.common.tabbedpane;

import com.mz.sshclient.ui.laf.metal.CustomMetalTabbedPaneUIDecorator;

import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CustomTabbedPaneClosable2 extends AbstractCustomTabbedPaneCloseable {

    private CustomMetalTabbedPaneUIDecorator customMetalTabbedPaneUIDecorator;
    private PropertyChangeListener tabLayoutPolicyListener;

    private volatile boolean skipInvalidate = false;

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

    @Override
    public void setUI(TabbedPaneUI ui) {
        //if ("javax.swing.plaf.metal.MetalTabbedPaneUI".equals(ui.getClass().getName())) {
            if (this.customMetalTabbedPaneUIDecorator == null) {
                this.customMetalTabbedPaneUIDecorator = new CustomMetalTabbedPaneUIDecorator();
                ui = this.customMetalTabbedPaneUIDecorator;
            }
        //}
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
