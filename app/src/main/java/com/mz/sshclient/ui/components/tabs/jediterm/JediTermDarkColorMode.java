package com.mz.sshclient.ui.components.tabs.jediterm;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;

import java.awt.Color;

public class JediTermDarkColorMode extends AbstractJediTermColorMode {
    private static final int DEFAULT_COLOR_FOREGROUND = -3618616;
    private static final int DEFAULT_COLOR_BACKGROUND = -14144460;

    private static final int DEFAULT_COLOR_HREF_FOREGROUND = 15790320;
    private static final int DEFAULT_COLOR_HREF_BACKGROUND = 2632756;

    public final TerminalColor getTerminalColor(int rgb) {
        return TerminalColor.awt(new Color(rgb));
    }

    @Override
    public TextStyle getDefaultStyle() {
        final TerminalColor colorForeground = getTerminalColor(DEFAULT_COLOR_FOREGROUND);
        final TerminalColor colorBackground = getTerminalColor(DEFAULT_COLOR_BACKGROUND);

        return new TextStyle(colorForeground, colorBackground);
    }

    @Override
    public TextStyle getHyperlinkColor() {
        final TerminalColor colorForeground = getTerminalColor(DEFAULT_COLOR_HREF_FOREGROUND);
        final TerminalColor colorBackground = getTerminalColor(DEFAULT_COLOR_HREF_BACKGROUND);

        return new TextStyle(colorForeground, colorBackground);
    }
}
