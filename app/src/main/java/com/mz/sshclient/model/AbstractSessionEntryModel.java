package com.mz.sshclient.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractSessionEntryModel implements ISessionEntryModel {
    protected String id = "";
    protected String name = "";
}
