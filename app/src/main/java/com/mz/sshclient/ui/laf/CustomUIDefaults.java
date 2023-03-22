package com.mz.sshclient.ui.laf;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import java.util.HashMap;
import java.util.Map;

public class CustomUIDefaults {

    /*public static void init() {
        final UIDefaults uiDefaults = UIManager.getDefaults();
        initMacOSKeyStrokes(uiDefaults);
    }

    private static void initMacOSKeyStrokes(final UIDefaults uiDefaults) {
        if (System.getProperty("os.name", "").startsWith("Mac OS X")) {
            Object fieldInputMap = new UIDefaults.LazyInputMap(new Object[] {
                    "meta C", DefaultEditorKit.copyAction,
                    "meta V", DefaultEditorKit.pasteAction,
                    "meta X", DefaultEditorKit.cutAction,
                    "meta A", DefaultEditorKit.selectAllAction
            });
            Object[] defaultsFieldInputs = new Object[] {
                    "EditorPane.focusInputMap", fieldInputMap,
                    "FormattedTextField.focusInputMap", fieldInputMap,
                    "PasswordField.focusInputMap", fieldInputMap,
                    "TextField.focusInputMap", fieldInputMap,
                    "TextPane.focusInputMap", fieldInputMap,
                    "TextArea.focusInputMap", fieldInputMap,
                    "Table.ancestorInputMap", fieldInputMap,
                    "Tree.focusInputMap", fieldInputMap
            };
            uiDefaults.putDefaults(defaultsFieldInputs);
        }
    }*/

    private static final String[] FOCUS_INPUT_MAPS = {
            "EditorPane.focusInputMap",
            "FormattedTextField.focusInputMap",
            "PasswordField.focusInputMap",
            "TextField.focusInputMap",
            "TextPane.focusInputMap",
            "TextArea.focusInputMap",
            "Table.ancestorInputMap",
            "Tree.focusInputMap"
    };

    private static final Map<String, Object> map = new HashMap<>();

    public static void read() {
        if (System.getProperty("os.name", "").startsWith("Mac OS X")) {
            final UIDefaults uiDefaults = UIManager.getDefaults();
            for (String k : FOCUS_INPUT_MAPS) {
                map.put(k, uiDefaults.get(k));
            }
        }
    }

    public static void write() {
        if (System.getProperty("os.name", "").startsWith("Mac OS X")) {
            final UIDefaults uiDefaults = UIManager.getDefaults();
            map.keySet().stream().forEach((key) -> {
                uiDefaults.put(key, map.get(key));
            });
        }
    }

}
