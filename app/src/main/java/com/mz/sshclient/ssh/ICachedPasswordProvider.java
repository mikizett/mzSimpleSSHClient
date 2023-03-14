package com.mz.sshclient.ssh;

public interface ICachedPasswordProvider {

    char[] getCachedPassword();

    void cachePassword(char[] password);

    char[] getCachedPassPhrase();

    void setCachedPassPhrase(char[] cachedPassPhrase);

}
