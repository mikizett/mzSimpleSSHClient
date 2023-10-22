package com.mz.sshclient.ui.components.common.tabbedpane;

import com.mz.sshclient.ui.config.AppSettings;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;
import java.awt.Color;
import java.awt.Component;

public abstract class AbstractCustomTabbedPaneCloseable extends JTabbedPane {

    protected ClosableHeaderTabComponent closableHeaderTabComponent;

    public AbstractCustomTabbedPaneCloseable() {
        // Narrow the right gap added around tab component of L&F;
        // MetalLookAndFeel default insets are: (0, 9, 1, 9).
        UIManager.put("TabbedPane.tabInsets", new InsetsUIResource(0, 3, 1, 3));

        // Increase the gap between label and icon or button in SCROLL_TAB_LAYOUT
        //UIManager.put("TabbedPane.textIconGap", 4);

        // Separator lines can be shown between tabs
        UIManager.put("TabbedPane.showTabSeparators", true);

        if (AppSettings.isDarkMode()) {
            // To make the selected tab stand out, change the selected tab background
            UIManager.put("TabbedPane.selectedBackground", Color.decode("#557394"));
        } else {
            UIManager.put("TabbedPane.selectedBackground", Color.decode("#BFCDDB"));
        }

        setTabPlacement(TOP);
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
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

    public void setClosableHeaderTabAction(final Action action) {
        closableHeaderTabComponent.addCloseableHeaderAction(action);
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
}
