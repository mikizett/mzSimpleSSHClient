package com.mz.sshclient.ui.components.tabs.sftp;

import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ui.components.tabs.sftp.view.AddressBar;
import com.mz.sshclient.ui.components.tabs.sftp.view.DndTransferData;
import com.mz.sshclient.ui.components.tabs.sftp.view.FolderView;
import com.mz.sshclient.ui.components.tabs.sftp.view.FolderViewEventListener;
import com.mz.sshclient.ui.components.tabs.sftp.view.NavigationHistory;
import com.mz.sshclient.ui.components.tabs.sftp.view.OverflowMenuHandler;
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

public abstract class AbstractFileBrowserView extends JPanel implements FolderViewEventListener {

    public enum PanelOrientation {
        LEFT, RIGHT
    }

    private final NavigationHistory history = new NavigationHistory();

    private final OverflowMenuHandler overflowMenuHandler;

    protected AddressBar addressBar;
    protected FolderView folderView;
    protected String path;
    protected PanelOrientation orientation;

    protected FileBrowser fileBrowser;

    public AbstractFileBrowserView(PanelOrientation orientation, FileBrowser fileBrowser) {
        super(new BorderLayout());

        this.fileBrowser = fileBrowser;
        this.orientation = orientation;

        overflowMenuHandler = new OverflowMenuHandler(this, fileBrowser);

        createAddressBar();
        addressBar.addActionListener(e -> {
            String text = e.getActionCommand();
            if (PathUtils.isSamePath(this.path, text)) {
                return;
            }
            if (text != null && text.length() > 0) {
                addBack(this.path);
                render(text, true);
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

        JButton btnUp = new JButton();
        btnUp.addActionListener(upAction);
        // TODO: check font here
        //btnUp.setFont(App.skin.getIconFont());
        btnUp.setText("\uf062");

        JButton btnHome = new JButton();
        // TODO: check font here
        //btnHome.setFont(App.skin.getIconFont());
        btnHome.setText("\uf015");
        btnHome.addActionListener(e -> {
            addBack(this.path);
            home();
        });

        JButton btnReload = new JButton();
        btnReload.addActionListener(reloadAction);
        // TODO: check font here
        //btnReload.setFont(App.skin.getIconFont());
        btnReload.setText("\uf021");

        JButton btnMore = new JButton();
        // TODO: check font here
        //btnMore.setFont(App.skin.getIconFont());
        btnMore.setText("\uf142");
        btnMore.addActionListener(e -> {
            JPopupMenu popupMenu = overflowMenuHandler.getOverflowMenu();
            popupMenu.pack();
            Dimension d = popupMenu.getPreferredSize();
            int x = btnMore.getWidth() - d.width;
            int y = btnMore.getHeight();
            popupMenu.show(btnMore, x, y);
        });

        LayoutUtil.equalizeSize(btnMore, btnReload, btnUp, btnHome);

        Box smallToolbar = Box.createHorizontalBox();
        smallToolbar.add(Box.createHorizontalStrut(5));
        smallToolbar.setBorder(new EmptyBorder(3, 0, 3, 0));
        smallToolbar.add(btnUp);
        smallToolbar.add(btnHome);

        Box b2 = Box.createHorizontalBox();
        b2.add(btnReload);
        b2.setBorder(new EmptyBorder(3, 0, 3, 0));
        b2.add(btnReload);
        b2.add(btnMore);

        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.add(smallToolbar, BorderLayout.WEST);
        toolBar.add(addressBar);
        toolBar.add(b2, BorderLayout.EAST);
        toolBar.setBorder(new EmptyBorder(5, 0, 5, 5));

        add(toolBar, BorderLayout.NORTH);

        folderView = new FolderView(this, text -> this.fileBrowser.updateRemoteStatus(text));

        overflowMenuHandler.setFolderView(folderView);

        add(folderView);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "up");
        getActionMap().put("up", upAction);

        this.fileBrowser.registerForViewNotification(this);
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
        this.render(this.path, true /*false*/);
    }

    @Override
    public void addBack(String path) {
        history.addBack(path);
    }

}
