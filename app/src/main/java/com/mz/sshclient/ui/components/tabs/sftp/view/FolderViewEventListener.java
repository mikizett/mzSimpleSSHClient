package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

public interface FolderViewEventListener {

    void addBack(String path);

    void render(String path);

    void render(String path, boolean useCache);

    void openApp(FileInfo file);

    boolean createMenu(JPopupMenu popupMenu, FileInfo[] files);

    void install(JComponent c);

    void reload();

}
