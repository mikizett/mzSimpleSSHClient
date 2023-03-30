package com.mz.sshclient.ssh.sftp.filesystem;

import com.mz.sshclient.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInfo implements Serializable {

    private static final Pattern USER_REGEX = Pattern.compile("^[^\\s]+\\s+[^\\s]+\\s+([^\\s]+)\\s+([^\\s]+)");
    private final String name;
    private String path;
    private long size;
    private FileType type;
    private LocalDateTime lastModified;
    private LocalDateTime created;
    private int permission;
    private String protocol;
    private String permissionString;
    private String extra;

    private boolean hidden;
    private String user;

    public FileInfo(
            final String name,
            final String path,
            final long size,
            final FileType type,
            final long lastModified,
            final int permission,
            final String protocol,
            final String permissionString,
            final long created,
            final String extra,
            final boolean hidden
    ) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
        this.lastModified = Utils.toDateTime(lastModified);
        this.permission = permission;
        this.protocol = protocol;
        this.permissionString = permissionString;
        this.created = Utils.toDateTime(created);
        this.extra = extra;
        if (StringUtils.isNotBlank(extra)) {
            this.user = getUserName();
        }
        this.hidden = hidden;
    }

    private String getUserName() {
        try {
            if (StringUtils.isNotBlank(extra)) {
                Matcher matcher = USER_REGEX.matcher(this.extra);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = Utils.toDateTime(lastModified);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.path = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isDirectory() {
        return type == FileType.Directory || type == FileType.DirLink;
    }

}
