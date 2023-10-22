package com.mz.sshclient.model.appsettings;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class AppSettingsModel {
    private String[] jumpHosts;
    private String selectedJumpHost;
    private boolean darkMode;

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("{").append("\n")
                .append("  jumpHosts: [").append("\n");

        Arrays.stream(jumpHosts).forEach(item -> b.append("    ").append(item).append("\n"));

        b.append("  ]").append("\n")
                .append("  selectedJumpHost: ").append(selectedJumpHost).append("\n")
                .append("  darkMode: ").append(darkMode).append("\n")
                .append("}");

        return b.toString();
    }
}
