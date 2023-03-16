package com.mz.sshclient.ssh;

import net.schmizz.sshj.userauth.password.PasswordFinder;

public interface IPasswordFinderCallback extends PasswordFinder, ICachedPasswordProvider {}
