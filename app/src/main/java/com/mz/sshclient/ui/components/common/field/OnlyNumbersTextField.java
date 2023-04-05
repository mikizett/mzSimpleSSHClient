package com.mz.sshclient.ui.components.common.field;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class OnlyNumbersTextField extends JTextField {

    public OnlyNumbersTextField() {
        this(null, null, 0);
    }

    public OnlyNumbersTextField(String text) {
        this(null, text, 0);
    }

    public OnlyNumbersTextField(int columns) {
        this(null, null, columns);
    }

    public OnlyNumbersTextField(String text, int columns) {
        this(null, text, columns);
    }

    public OnlyNumbersTextField(Document doc, String text, int columns) {
        super(doc, text, columns);

        PlainDocument plainDocument = (PlainDocument) getDocument();
        if (plainDocument != null) {
            plainDocument.setDocumentFilter(new NumberFilter());
        }
    }

    /**
     * Document filter which allows only numbers
     */
    private static class NumberFilter extends DocumentFilter {

        private enum FilterType {
            INSERT, REPLACE, REMOVE
        };

        @Override
        public void insertString(FilterBypass filterBypass, int offset, String text, AttributeSet attr) throws BadLocationException {
            validation(FilterType.INSERT, filterBypass, offset, -1, text, attr);
        }

        @Override
        public void replace(FilterBypass filterBypass, int offset, int length, String text, AttributeSet attr) throws BadLocationException {
            validation(FilterType.REPLACE, filterBypass, offset, length, text, attr);
        }

        @Override
        public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException {
            validation(FilterType.REMOVE, filterBypass, offset, length, null, null);
        }

        private void validation(FilterType filterType, FilterBypass fb, int offset, int len, String text, AttributeSet attr) throws BadLocationException {
            if (text.startsWith("0")) {
                return;
            }

            Document doc = fb.getDocument();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(doc.getText(0, doc.getLength()));

            textAdaption(filterType, stringBuilder, offset, len, text);

            if (validateText(stringBuilder.toString())) {
                textAction(filterType, fb, offset, len, text, attr);
            } else {
                // warn the user and don't allow the insert
            }
        }

        private boolean validateText(String text) {
            try {
                Integer.parseInt(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private void textAdaption(FilterType filterType, StringBuilder stringBuilder, int offset, int len, String text) {
            switch (filterType) {
                case INSERT:
                    stringBuilder.insert(offset, text);
                    break;
                case REPLACE:
                    //stringBuilder.replace(offset, offset + len, text);
                    stringBuilder.replace(offset, offset, text);
                    break;
                case REMOVE:
                    //stringBuilder.delete(offset, offset + len);
                    stringBuilder.delete(offset, offset);
                    break;
                default:
                    break;
            }
        }

        private void textAction(
                FilterType filterType,
                FilterBypass filterBypass,
                int offset,
                int len,
                String text,
                AttributeSet attr
        ) throws BadLocationException {

            switch (filterType) {
                case INSERT:
                    super.insertString(filterBypass, offset, text, attr);
                    break;
                case REPLACE:
                    super.replace(filterBypass, offset, len, text, attr);
                    break;
                case REMOVE:
                    super.remove(filterBypass, offset, len);
                    break;
                default:
                    break;
            }
        }
    } // end inner class

}
