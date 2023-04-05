package com.mz.sshclient.ui.components.tabs.sftp;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ui.components.tabs.sftp.view.AddressBar;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferData;
import com.mz.sshclient.ui.components.tabs.sftp.view.IFileBrowserEventListener;
import com.mz.sshclient.ui.components.tabs.sftp.view.FileBrowserPanel;
import com.mz.sshclient.ui.components.tabs.sftp.view.NavigationHistory;
import com.mz.sshclient.ui.components.tabs.sftp.view.OverflowMenuHandler;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import com.mz.sshclient.utils.LayoutUtil;
import com.mz.sshclient.utils.PathUtils;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public abstract class AbstractIFileBrowserView extends JPanel implements IFileBrowserEventListener {

    public enum PanelOrientation {
        LEFT, RIGHT
    }

    private final NavigationHistory history = new NavigationHistory();

    private final OverflowMenuHandler overflowMenuHandler;

    protected AddressBar addressBar;
    protected FileBrowserPanel fileBrowserPanel;
    protected String path;
    protected PanelOrientation orientation;

    protected FileBrowser fileBrowser;

    public AbstractIFileBrowserView(PanelOrientation orientation, FileBrowser fileBrowser) {
        super(new BorderLayout());

        this.fileBrowser = fileBrowser;
        this.orientation = orientation;

        overflowMenuHandler = new OverflowMenuHandler(this, fileBrowser);

        createAddressBar();

        addressBar.addActionListener(e -> {
            String text = e.getActionCommand();
            if (PathUtils.isSamePath(path, text)) {
                return;
            }
            if (text != null && text.length() > 0) {
                final File file = new File(text);
                if (!file.exists()) {
                    MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, "Path doesn't exist!");
                } else {
                    addBack(path);
                    render(text, true);
                }
            }
        });

        AbstractAction upAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBack(path);
                up();
            }
        };
        AbstractAction reloadAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        };

        JButton buttonUp = new JButton();
        buttonUp.addActionListener(upAction);
        buttonUp.setText("\uf062");

        JButton buttonHome = new JButton();
        buttonHome.setText("\uf015");
        buttonHome.addActionListener(e -> {
            addBack(path);
            home();
        });

        JButton buttonReload = new JButton();
        buttonReload.addActionListener(reloadAction);
        buttonReload.setText("\uf021");

        JButton buttonMore = new JButton();
        buttonMore.setText("\uf142");
        buttonMore.addActionListener(e -> {
            JPopupMenu popupMenu = overflowMenuHandler.getOverflowMenu();
            popupMenu.pack();
            Dimension d = popupMenu.getPreferredSize();
            int x = buttonMore.getWidth() - d.width;
            int y = buttonMore.getHeight();
            popupMenu.show(buttonMore, x, y);
        });

        LayoutUtil.equalizeSize(buttonMore, buttonReload, buttonUp, buttonHome);

        Box smallToolbar = Box.createHorizontalBox();
        smallToolbar.add(Box.createHorizontalStrut(5));
        smallToolbar.setBorder(new EmptyBorder(3, 0, 3, 0));
        smallToolbar.add(buttonUp);
        smallToolbar.add(buttonHome);

        Box b2 = Box.createHorizontalBox();
        b2.add(buttonReload);
        b2.setBorder(new EmptyBorder(3, 0, 3, 0));
        b2.add(buttonReload);
        b2.add(buttonMore);

        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.add(smallToolbar, BorderLayout.WEST);
        toolBar.add(addressBar);
        toolBar.add(b2, BorderLayout.EAST);
        toolBar.setBorder(new EmptyBorder(5, 0, 5, 5));

        add(toolBar, BorderLayout.NORTH);

        fileBrowserPanel = new FileBrowserPanel(this);

        overflowMenuHandler.setFolderView(fileBrowserPanel);

        add(fileBrowserPanel);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "up");
        getActionMap().put("up", upAction);

        fileBrowser.registerForViewNotification(this);
    }

    protected abstract void createAddressBar();

    public abstract String getHostText();

    public abstract String getPathText();

    @Override
    public abstract String toString();

    public boolean close() {
        this.fileBrowser.unRegisterForViewNotification(this);
        return true;
    }

    public String getCurrentDirectory() {
        return this.path;
    }

    public abstract boolean handleDrop(DndTransferData transferData);

    protected abstract void up();

    protected abstract void home();

    public abstract IFileSystem getFileSystem() throws Exception;

    @Override
    public void reload() {
        render(path, true);
    }

    @Override
    public void addBack(String path) {
        history.addBack(path);
    }

}
