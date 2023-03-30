package com.mz.sshclient.ssh.sftp.filesystem.sftp;

import com.mz.sshclient.ssh.SshClient;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ssh.sftp.SshRemoteFileInputStream;
import com.mz.sshclient.ssh.sftp.SshRemoteFileOutputStream;
import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.InputTransferChannel;
import com.mz.sshclient.ssh.sftp.filesystem.OutputTransferChannel;
import com.mz.sshclient.utils.PathUtils;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.PacketType;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.xfer.FilePermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SshFileSystem implements IFileSystem {

    private static final Logger LOG = LogManager.getLogger(SshFileSystem.class);

    public static final String PROTO_SFTP = "sftp";
    private SFTPClient sftp;
    private final SshClient ssh;
    private String home;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public SshFileSystem(SshClient ssh) {
        this.ssh = ssh;
    }

    public SFTPClient getSftp() throws Exception {
        ensureConnected();
        return sftp;
    }

    private void ensureConnected() throws Exception {
        if (closed.get()) {
            this.ssh.close();
            throw new SshOperationCanceledException("Connection closed");
        }
        if (!ssh.isConnected()) {
            ssh.connect();
        }
        if (sftp == null) {
            this.sftp = ssh.createSftpClient();
            this.sftp.getSFTPEngine().getSubsystem().setAutoExpand(true);
        }
    }

    @Override
    public void delete(FileInfo f) throws Exception {
        synchronized (this.ssh) {
            ensureConnected();
            try {
                if (f.getType() == FileType.Directory) {
                    List<FileInfo> list = list(f.getPath());
                    if (list != null && !list.isEmpty()) {
                        for (FileInfo fc : list) {
                            delete(fc);
                        }
                    }
                    this.sftp.rmdir(f.getPath());
                } else {
                    this.sftp.rm(f.getPath());
                }
            } catch (SFTPException e) {
                LOG.error(e);
                if (e.getStatusCode() == Response.StatusCode.PERMISSION_DENIED) {
                    throw new AccessDeniedException("Access denied");
                }
            }
        }
    }

    @Override
    public void chmod(int perm, String path) throws Exception {
        synchronized (this.ssh) {
            ensureConnected();
            try {
                this.sftp.chmod(path, perm);
            } catch (SFTPException e) {
                LOG.error(e);
                if (e.getStatusCode() == Response.StatusCode.PERMISSION_DENIED) {
                    throw new AccessDeniedException("Access denied");
                }
                throw e;
            }
        }
    }

    @Override
    public List<FileInfo> list(String path) throws Exception {
        synchronized (this.ssh) {
            ensureConnected();
            return listFiles(path);
        }
    }

    private FileInfo resolveSymlink(String name, String pathToResolve, FileAttributes attrs, String longName) throws Exception {
        try {
            while (true) {
                String str = sftp.readlink(pathToResolve);
                LOG.debug("Read symlink: " + pathToResolve + "=" + str);
                LOG.debug("Getting link attrs: " + pathToResolve);
                attrs = sftp.stat(pathToResolve);

                if (attrs.getType() != FileMode.Type.SYMLINK) {
                    return new FileInfo(name, pathToResolve,
                            (attrs.getType() == FileMode.Type.DIRECTORY ? -1 : attrs.getSize()),
                            attrs.getType() == FileMode.Type.DIRECTORY ? FileType.DirLink : FileType.FileLink,
                            attrs.getMtime() * 1000, FilePermission.toMask(attrs.getPermissions()), PROTO_SFTP,
                            getPermissionStr(attrs.getPermissions()), attrs.getAtime(), longName, name.startsWith("."));
                }
            }
        } catch (SFTPException e) {
            if (e.getStatusCode() == Response.StatusCode.NO_SUCH_FILE
                    || e.getStatusCode() == Response.StatusCode.NO_SUCH_PATH
                    || e.getStatusCode() == Response.StatusCode.PERMISSION_DENIED) {
                return new FileInfo(name, pathToResolve, 0, FileType.FileLink, attrs.getMtime() * 1000,
                        FilePermission.toMask(attrs.getPermissions()), PROTO_SFTP,
                        getPermissionStr(attrs.getPermissions()), attrs.getAtime(), longName, name.startsWith("."));
            }
            throw e;
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }
    }

    private List<FileInfo> listFiles(String path) throws Exception {
        synchronized (this.ssh) {
            List<FileInfo> childs = new ArrayList<>();
            try {
                if (path == null || path.length() < 1) {
                    path = this.getHome();
                }
                List<RemoteResourceInfoWrapper> files = ls(path);
                if (!files.isEmpty()) {
                    for (int i = 0; i < files.size(); i++) {
                        RemoteResourceInfo ent = files.get(i).getInfo();
                        String longName = files.get(i).getLongPath();

                        FileAttributes attrs = ent.getAttributes();

                        if (attrs.getType() == FileMode.Type.SYMLINK) {
                            try {
                                childs.add(resolveSymlink(ent.getName(), ent.getPath(), attrs, longName));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            FileInfo e = new FileInfo(ent.getName(), ent.getPath(),
                                    (ent.isDirectory() ? -1 : attrs.getSize()),
                                    ent.isDirectory() ? FileType.Directory : FileType.File, attrs.getMtime() * 1000,
                                    net.schmizz.sshj.xfer.FilePermission.toMask(attrs.getPermissions()), PROTO_SFTP,
                                    getPermissionStr(attrs.getPermissions()), attrs.getAtime(), longName,
                                    ent.getName().startsWith("."));
                            childs.add(e);
                        }
                    }
                }
            } catch (SFTPException e) {
                e.printStackTrace();
                if (e.getStatusCode() == Response.StatusCode.NO_SUCH_FILE
                        || e.getStatusCode() == Response.StatusCode.NO_SUCH_PATH) {
                    throw new FileNotFoundException(path);
                }
                if (e.getStatusCode() == Response.StatusCode.PERMISSION_DENIED) {
                    throw new AccessDeniedException(path);
                }
            } catch (Exception e) {
                LOG.error(e);
                throw new IOException(e);
            }
            return childs;
        }
    }

    @Override
    public void close() throws Exception {
        this.closed.set(true);
        this.sftp.close();
    }

    @Override
    public String getHome() throws Exception {
        LOG.debug("Getting home directory... on " + Thread.currentThread().getName());
        if (home != null) {
            return home;
        }

        synchronized (ssh) {
            LOG.debug("Getting home directory");
            ensureConnected();
            this.home = sftp.canonicalize("");
            return this.home;
        }
    }

    @Override
    public String[] getRoots() throws Exception {
        return new String[] { "/" };
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public FileInfo getInfo(String path) throws Exception {
        synchronized (ssh) {
            ensureConnected();
            try {
                FileAttributes attrs = sftp.stat(path);
                if (attrs.getType() == FileMode.Type.SYMLINK) {
                    return resolveSymlink(PathUtils.getFileName(path), path, attrs, null);
                } else {
                    String name = PathUtils.getFileName(path);
                    return new FileInfo(name, path, (attrs.getType() == FileMode.Type.DIRECTORY ? -1 : attrs.getSize()),
                            attrs.getType() == FileMode.Type.DIRECTORY ? FileType.Directory : FileType.File,
                            attrs.getMtime() * 1000, FilePermission.toMask(attrs.getPermissions()), PROTO_SFTP,
                            getPermissionStr(attrs.getPermissions()), attrs.getAtime(), null, name.startsWith("."));
                }
            } catch (SFTPException e) {
                LOG.error(e);
                if (e.getStatusCode() == Response.StatusCode.NO_SUCH_FILE
                        || e.getStatusCode() == Response.StatusCode.NO_SUCH_PATH) {
                    throw new FileNotFoundException(path);
                }
                throw e;
            }
        }
    }

    @Override
    public void createLink(String src, String dst, boolean hardLink) throws Exception {
        synchronized (ssh) {
            ensureConnected();
            if (hardLink) {
                throw new IOException("Not implemented");
            } else {
                this.sftp.symlink(src, dst);
            }
        }
    }

    @Override
    public String getName() {
        return ssh.getSessionItemModel().getName();
    }

    @Override
    public void deleteFile(String f) throws Exception {
        synchronized (ssh) {
            ensureConnected();
            this.sftp.rm(f);
        }
    }

    @Override
    public void createFile(String path) throws Exception {
        synchronized (ssh) {
            ensureConnected();
            try {
                sftp.open(path, EnumSet.of(OpenMode.APPEND, OpenMode.CREAT)).close();
            } catch (SFTPException e) {
                if (e.getStatusCode() == Response.StatusCode.PERMISSION_DENIED) {
                    throw new AccessDeniedException(path);
                }
            } catch (Exception e) {
                LOG.error(e);
                if (ssh.isConnected()) {
                    throw new FileNotFoundException(e.getMessage());
                }
                throw new Exception(e);
            }
        }
    }

    @Override
    public void rename(String oldName, String newName) throws Exception {
        synchronized (ssh) {
            try {
                ensureConnected();
                sftp.rename(oldName, newName);
            } catch (SFTPException e) {
                LOG.error(e);
                if (e.getStatusCode() == Response.StatusCode.PERMISSION_DENIED) {
                    throw new AccessDeniedException(oldName);
                }
            } catch (Exception e) {
                LOG.error(e);
                if (ssh.isConnected()) {
                    throw new FileNotFoundException(e.getMessage());
                }
                throw new Exception(e);
            }
        }
    }

    @Override
    public void mkdir(String path) throws Exception {
        synchronized (ssh) {
            ensureConnected();
            try {
                sftp.mkdir(path);
            } catch (SFTPException e) {
                if (e.getStatusCode() == Response.StatusCode.PERMISSION_DENIED) {
                    throw new AccessDeniedException(path);
                }
            } catch (Exception e) {
                LOG.error(e);
                if (ssh.isConnected()) {
                    throw new FileNotFoundException(e.getMessage());
                }
                throw new Exception(e);
            }
        }
    }

    @Override
    public boolean mkdirs(String absPath) throws Exception {
        synchronized (ssh) {
            ensureConnected();
            if (absPath.equals("/")) {
                return true;
            }

            try {
                sftp.stat(absPath);
                return false;
            } catch (Exception e) {
                if (!ssh.isConnected()) {
                    throw e;
                }
            }

            String parent = PathUtils.getParent(absPath);

            mkdirs(parent);
            sftp.mkdir(absPath);

            return true;
        }
    }

    @Override
    public long getAllFiles(String dir, String baseDir, Map<String, String> fileMap, Map<String, String> folderMap) throws Exception {
        synchronized (ssh) {
            ensureConnected();
            long size = 0;
            String parentFolder = PathUtils.combine(baseDir, PathUtils.getFileName(dir), File.separator);

            folderMap.put(dir, parentFolder);

            List<FileInfo> list = list(dir);
            for (FileInfo f : list) {
                if (f.getType() == FileType.Directory) {
                    folderMap.put(f.getPath(), PathUtils.combine(parentFolder, f.getName(), File.separator));
                    size += getAllFiles(f.getPath(), parentFolder, fileMap, folderMap);
                } else {
                    fileMap.put(f.getPath(), PathUtils.combine(parentFolder, f.getName(), File.separator));
                    size += f.getSize();
                }
            }
            return size;
        }
    }

    @Override
    public boolean isConnected() {
        return !closed.get() && ssh.isConnected();
    }

    @Override
    public String getProtocol() {
        return PROTO_SFTP;
    }


    public InputTransferChannel inputTransferChannel() throws Exception {
        synchronized (ssh) {
            ensureConnected();
            try {
                return new InputTransferChannel() {
                    @Override
                    public InputStream getInputStream(String path) throws Exception {
                        RemoteFile remoteFile = sftp.open(path, EnumSet.of(OpenMode.READ));
                        return new SshRemoteFileInputStream(remoteFile,
                                sftp.getSFTPEngine().getSubsystem().getLocalMaxPacketSize());
                    }

                    @Override
                    public String getSeparator() {
                        return "/";
                    }

                    @Override
                    public long getSize(String path) throws Exception {
                        return getInfo(path).getSize();
                    }
                };
            } catch (Exception e) {
                LOG.error(e);
                if (ssh.isConnected()) {
                    throw new FileNotFoundException();
                }
                throw new Exception();
            }
        }
    }

    public OutputTransferChannel outputTransferChannel() throws Exception {
        synchronized (ssh) {
            ensureConnected();
            try {
                return new OutputTransferChannel() {
                    @Override
                    public OutputStream getOutputStream(String path) throws Exception {
                        try {
                            RemoteFile remoteFile = sftp.open(path,
                                    EnumSet.of(OpenMode.WRITE, OpenMode.TRUNC, OpenMode.CREAT));
                            return new SshRemoteFileOutputStream(remoteFile,
                                    sftp.getSFTPEngine().getSubsystem().getRemoteMaxPacketSize());
                        } catch (SFTPException e) {
                            LOG.error(e);
                            if (e.getStatusCode() == Response.StatusCode.PERMISSION_DENIED) {
                                throw new AccessDeniedException(e.getMessage());
                            }
                            throw e;
                        }
                    }

                    @Override
                    public String getSeparator() {
                        return "/";
                    }
                };
            } catch (Exception e) {
                LOG.error(e);
                if (ssh.isConnected()) {
                    throw new FileNotFoundException();
                }
                throw new Exception();
            }
        }
    }

    public String getSeparator() {
        return "/";
    }

    private List<RemoteResourceInfoWrapper> ls(String path) throws Exception {
        final SFTPEngine requester = sftp.getSFTPEngine();
        final byte[] handle = requester
                .request(requester.newRequest(PacketType.OPENDIR).putString(path,
                        requester.getSubsystem().getRemoteCharset()))
                .retrieve(requester.getTimeoutMs(), TimeUnit.MILLISECONDS).ensurePacketTypeIs(PacketType.HANDLE)
                .readBytes();
        try (ExtendedRemoteDirectory dir = new ExtendedRemoteDirectory(requester, path, handle)) {
            return dir.scanExtended(null);
        }
    }

    private String getPermissionStr(Set<FilePermission> perms) {
        char[] arr = {'-', '-', '-', '-', '-', '-', '-', '-', '-'};
        if (perms.contains(FilePermission.USR_R)) {
            arr[0] = 'r';
        }
        if (perms.contains(FilePermission.USR_W)) {
            arr[1] = 'w';
        }
        if (perms.contains(FilePermission.USR_X)) {
            arr[2] = 'x';
        }
        if (perms.contains(FilePermission.GRP_R)) {
            arr[3] = 'r';
        }
        if (perms.contains(FilePermission.GRP_W)) {
            arr[4] = 'w';
        }
        if (perms.contains(FilePermission.GRP_X)) {
            arr[5] = 'x';
        }
        if (perms.contains(FilePermission.OTH_R)) {
            arr[6] = 'r';
        }
        if (perms.contains(FilePermission.OTH_W)) {
            arr[7] = 'w';
        }
        if (perms.contains(FilePermission.OTH_W)) {
            arr[8] = 'x';
        }
        return new String(arr);
    }

}
