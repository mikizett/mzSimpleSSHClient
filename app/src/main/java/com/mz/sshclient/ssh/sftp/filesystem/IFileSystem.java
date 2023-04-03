package com.mz.sshclient.ssh.sftp.filesystem;

import java.util.List;
import java.util.Map;

public interface IFileSystem extends AutoCloseable {

    FileInfo getInfo(String path) throws Exception;

    List<FileInfo> list(String path) throws Exception;

    String getHome() throws Exception;

    boolean isLocal();

    void rename(String oldName, String newName) throws Exception;

    void delete(FileInfo f) throws Exception;

    void deleteFile(String f) throws Exception;

    void mkdir(String path) throws Exception;

    void close() throws Exception;

    boolean isConnected();

    void chmod(int perm, String path) throws Exception;

    boolean mkdirs(String absPath) throws Exception;

    long getAllFiles(String dir, String baseDir, Map<String, String> fileMap, Map<String, String> folderMap) throws Exception;

    String getProtocol();

    void createFile(String path) throws Exception;

    String[] getRoots();

    void createLink(String src, String dst, boolean hardLink) throws Exception;

    String getName();

    InputTransferChannel inputTransferChannel() throws Exception;

    OutputTransferChannel outputTransferChannel() throws Exception;

    String getSeparator();

}
