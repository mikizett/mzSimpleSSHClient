package com.mz.sshclient.ui.laf;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

public class CustomUIDefaults {

    public static void init() {
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
    }

}
