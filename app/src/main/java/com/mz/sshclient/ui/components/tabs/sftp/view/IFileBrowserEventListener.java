package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;

import javax.swing.JPopupMenu;

public interface IFileBrowserEventListener {

    void addBack(String path);

    void render(String path);

    void render(String path, boolean useCache);

    boolean createMenu(JPopupMenu popupMenu, FileInfo[] files);

    void reload();

}
