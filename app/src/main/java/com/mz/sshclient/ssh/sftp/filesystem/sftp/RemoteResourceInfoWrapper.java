package com.mz.sshclient.ssh.sftp.filesystem.sftp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;

@Getter
@Setter
@AllArgsConstructor
public class RemoteResourceInfoWrapper {

    private RemoteResourceInfo info;
    private String longPath;

}
