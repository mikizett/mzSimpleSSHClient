package com.mz.sshclient.ssh.sftp.filesystem.sftp;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.PacketType;
import net.schmizz.sshj.sftp.PathComponents;
import net.schmizz.sshj.sftp.RemoteDirectory;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExtendedRemoteDirectory extends RemoteDirectory {

    public ExtendedRemoteDirectory(SFTPEngine requester, String path, byte[] handle) {
        super(requester, path, handle);
    }

    public List<RemoteResourceInfoWrapper> scanExtended(RemoteResourceFilter filter) throws IOException {
        List<RemoteResourceInfoWrapper> remoteResourceInfoWrappers = new ArrayList<>(0);
        boolean finished;

        while (true) {
            final Response res = requester
                    .request(newRequest(PacketType.READDIR))
                    .retrieve(requester.getTimeoutMs(), TimeUnit.MILLISECONDS);

            finished = readRemoteFileAttributes(res, filter, remoteResourceInfoWrappers);

            if (finished) {
                break;
            }
        }
        return remoteResourceInfoWrappers;
    }

    private boolean readRemoteFileAttributes(
            final Response res,
            final RemoteResourceFilter filter,
            final List<RemoteResourceInfoWrapper> remoteResourceInfoWrappers
    ) throws IOException {

        switch (res.getType()) {
            case NAME:
                final int count = res.readUInt32AsInt();
                for (int i = 0; i < count; i++) {
                    final String name = res.readString(requester.getSubsystem().getRemoteCharset());
                    final String longName = res.readString();

                    final FileAttributes attrs = res.readFileAttributes();
                    final PathComponents comps = requester.getPathHelper().getComponents(path, name);
                    final RemoteResourceInfo inf = new RemoteResourceInfo(comps, attrs);
                    final RemoteResourceInfoWrapper wri = new RemoteResourceInfoWrapper(inf, longName);
                    if (!(".".equals(name) || "..".equals(name)) && (filter == null || filter.accept(inf))) {
                        remoteResourceInfoWrappers.add(wri);
                    }
                }
                break;
            case STATUS:
                res.ensureStatusIs(Response.StatusCode.EOF);
                return true;
            default:
                throw new SFTPException("Unexpected packet: " + res.getType());
        }
        return false;
    }

}
