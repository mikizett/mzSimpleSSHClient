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

    public static void update() {
        OVERWRITE.setValue("Overwrite");
        AUTORENAME.setValue("Autorename");
        SKIP.setValue("Skip");
        PROMPT.setValue("Prompt");
        CANCEL.setValue("Cancel");
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String pValue) {
        this.value = pValue;
    }

    @Override
    public String toString() {
        return value;
    }

}
