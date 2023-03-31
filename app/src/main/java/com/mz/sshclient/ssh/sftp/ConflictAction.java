package com.mz.sshclient.ssh.sftp;

public enum ConflictAction {

    OVERWRITE(0, "Overwrite"),
    AUTORENAME(1, "Autorename"),
    SKIP(2, "Skip"),
    PROMPT(3, "Prompt"),
    CANCEL(4, "Cancel")
    ;

    private final int key;
    private String value;

    ConflictAction(int pKey, String pValue) {
        this.key = pKey;
        this.value = pValue;
    }

    public int getKey() {
        return key;
    }

    @Override
    public String toString() {
        return value;
    }

}
