package com.mz.sshclient.ui.components.tabs.sftp.view;

import com.sun.jna.platform.FileMonitor;
import com.sun.jna.platform.win32.W32FileMonitor;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Win32DragHandler {

    private final FileMonitor fileMonitor = new W32FileMonitor();

    public synchronized void listenForDrop(String keyToListen, Consumer<File> callback) {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        for (File drive : File.listRoots()) {
            if (fsv.isDrive(drive)) {
                try {
                    fileMonitor.addWatch(drive, W32FileMonitor.FILE_RENAMED | W32FileMonitor.FILE_CREATED, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        fileMonitor.addFileListener(e -> {
            File file = e.getFile();
            if (file.getName().startsWith(keyToListen)) {
                callback.accept(file);
            }
        });
    }

    public synchronized void dispose() {
        this.fileMonitor.dispose();
    }

}
