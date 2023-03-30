package com.mz.sshclient.ssh.sftp.filesystem.local;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.InputTransferChannel;
import com.mz.sshclient.ssh.sftp.filesystem.OutputTransferChannel;
import com.mz.sshclient.utils.PathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalFileSystem implements IFileSystem {

    public static final String PROTOCOL_LOCAL_FILE = "local";

    @Override
    public void chmod(int perm, String path) throws Exception {
    }

    @Override
    public FileInfo getInfo(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            throw new FileNotFoundException(path);
        }
        Path p = f.toPath();
        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
        return new FileInfo(f.getName(), path, f.length(),
                f.isDirectory() ? FileType.Directory : FileType.File, f.lastModified(), -1, PROTOCOL_LOCAL_FILE, "",
                attrs.creationTime().toMillis(), "", f.isHidden());
    }

    @Override
    public String getHome() throws IOException {
        return System.getProperty("user.home");
    }

    @Override
    public List<FileInfo> list(String path) throws Exception {
        if (path == null || path.length() < 1) {
            path = System.getProperty("user.home");
        }
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File[] childs = new File(path).listFiles();
        List<FileInfo> list = new ArrayList<>();
        if (childs == null || childs.length < 1) {
            return list;
        }
        for (File f : childs) {
            if (!f.exists() && !f.canRead()) {
                continue;
            }
            try {
                Path p = f.toPath();
                BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                FileInfo info = new FileInfo(f.getName(), f.getAbsolutePath(), f.length(),
                        f.isDirectory() ? FileType.Directory : FileType.File, f.lastModified(), -1, PROTOCOL_LOCAL_FILE,
                        "", attrs.creationTime().toMillis(), "", f.isHidden());
                list.add(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public void rename(String oldName, String newName) throws Exception {
        if (!new File(oldName).renameTo(new File(newName))) {
            throw new FileNotFoundException();
        }
    }

    @Override
    public synchronized void delete(FileInfo f) throws Exception {
        if (f.getType() == FileType.Directory) {
            List<FileInfo> list = list(f.getPath());
            if (list != null && list.size() > 0) {
                for (FileInfo fc : list) {
                    delete(fc);
                }
            }
            new File(f.getPath()).delete();
        } else {
            new File(f.getPath()).delete();
        }
    }

    @Override
    public void mkdir(String path) throws Exception {
        new File(path).mkdirs();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean mkdirs(String absPath) throws Exception {
        return new File(absPath).mkdirs();
    }

    @Override
    public long getAllFiles(String dir, String baseDir, Map<String, String> fileMap, Map<String, String> folderMap) throws Exception {
        long size = 0;
        String parentFolder = PathUtils.combineUnix(baseDir, PathUtils.getFileName(dir));

        folderMap.put(dir, parentFolder);

        List<FileInfo> list = list(dir);
        for (FileInfo f : list) {
            if (f.getType() == FileType.Directory) {
                folderMap.put(f.getPath(), PathUtils.combineUnix(parentFolder, f.getName()));
                size += getAllFiles(f.getPath(), parentFolder, fileMap, folderMap);
            } else {
                fileMap.put(f.getPath(), PathUtils.combineUnix(parentFolder, f.getName()));
                size += f.getSize();
            }
        }
        return size;
    }

    @Override
    public void deleteFile(String f) throws Exception {
        new File(f).delete();
    }

    @Override
    public String getProtocol() {
        return PROTOCOL_LOCAL_FILE;
    }

    @Override
    public void createFile(String path) throws Exception {
        Files.createFile(Paths.get(path));
    }

    @Override
    public void createLink(String src, String dst, boolean hardLink) throws Exception {
    }

    @Override
    public String getName() {
        return "Local files";
    }

    @Override
    public String[] getRoots() throws Exception {
        File[] roots = File.listRoots();
        String[] arr = new String[roots.length];
        int i = 0;
        for (File f : roots) {
            arr[i++] = f.getAbsolutePath();
        }
        return arr;
    }

    @Override
    public InputTransferChannel inputTransferChannel() throws Exception {
        return new InputTransferChannel() {
            @Override
            public InputStream getInputStream(String path) throws Exception {
                return new FileInputStream(path);
            }

            @Override
            public String getSeparator() {
                return File.separator;
            }

            @Override
            public long getSize(String path) throws Exception {
                return getInfo(path).getSize();
            }
        };
    }

    @Override
    public OutputTransferChannel outputTransferChannel() throws Exception {
        return new OutputTransferChannel() {
            @Override
            public OutputStream getOutputStream(String path) throws Exception {
                return new FileOutputStream(path);
            }

            @Override
            public String getSeparator() {
                return File.separator;
            }
        };
    }

    @Override
    public String getSeparator() {
        return File.separator;
    }

}
