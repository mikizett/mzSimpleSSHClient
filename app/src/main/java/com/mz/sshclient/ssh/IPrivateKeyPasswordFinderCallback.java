package com.mz.sshclient.ssh;

import net.schmizz.sshj.userauth.password.PasswordFinder;

public interface IPrivateKeyPasswordFinderCallback extends PasswordFinder {

    char[] getEncodedCachedPassword();

}
