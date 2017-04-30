import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PriceDocumentListener implements DocumentListener {
    JTextField currentField;
    Pattern pricePattern = Pattern.compile("^\\d+?(\\.\\d{0,2}$|$)");
    Matcher priceMatcher;

    public PriceDocumentListener(JTextField field) {
        currentField = field;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        System.out.println("change");
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        System.out.println("remove");
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        String tempText = null;
        try {
            tempText = e.getDocument().getText(0,e.getDocument().getLength());
            if (tempText.length() > 0) {
                priceMatcher = pricePattern.matcher(tempText);
                if (!priceMatcher.matches()) {
                    try {
                        String originalString = currentField.getText(0,e.getOffset()) + currentField.getText(e.getOffset() + e.getLength(),currentField.getText().length() - (e.getOffset() + e.getLength()));
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                currentField.setText(originalString);
                            }
                        });

                    } catch (IllegalStateException ise) {
                        ise.printStackTrace();
                    }
                }
            }
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

}