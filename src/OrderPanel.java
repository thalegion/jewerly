import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by User on 30.04.2017.
 */
public class OrderPanel extends JPanel {

    private Order order;

    private JComboBox clientField = new JComboBox();
    private JComboBox statusField = new JComboBox();
    private JComboBox paymentStatusField = new JComboBox();

    private JTextField priceField = new JTextField("",15);

    private JButton saveButton = new JButton("Сохранить");
    private JButton cancelButton = new JButton("Отмена");

    private boolean saved = false;

    public OrderPanel() {
        this.order = new Order();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,5);

        clientField.addItem(new ComboItem());
        ResultSet fillSet = null;
        try {
            fillSet = main.db.select("*","clients","","name ASC","");
            while (fillSet.next()) {
                clientField.addItem(new ComboItem(fillSet,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        fillSet = null;
        try {
            fillSet = main.db.select("*","orders_statuses","","name ASC","");
            while (fillSet.next()) {
                statusField.addItem(new ComboItem(fillSet,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        fillSet = null;
        try {
            fillSet = main.db.select("*","payment_statuses","","name ASC","");
            while (fillSet.next()) {
                paymentStatusField.addItem(new ComboItem(fillSet,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Клиент*:"),c);
        c.gridx = 1;
        add(clientField,c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Статус:"),c);
        c.gridx = 1;
        add(statusField,c);

        c.gridx = 0;
        c.gridy = 2;
        add(new JLabel("Статус оплаты:"),c);
        c.gridx = 1;
        add(paymentStatusField,c);

        c.gridx = 0;
        c.gridy = 3;
        add(saveButton,c);
        c.gridy = 4;
        add(cancelButton,c);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveOrder();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDialog();
            }
        });
    }

   /* public ClientPanel (Client cln) {
        client = cln;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,5);

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("ФИО*:"),c);
        c.gridx = 1;
        nameField.setText(client.getName());
        add(nameField,c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Телефон*:"),c);
        c.gridx = 1;
        phoneField.setText(client.getPhone());
        add(phoneField,c);

        c.gridx = 0;
        c.gridy = 3;
        add(saveButton,c);
        c.gridy = 4;
        add(cancelButton,c);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveOrder();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDialog();
            }
        });
    }*/

    protected boolean isSaved() {
        return saved;
    }

    protected Order getOrder() {
        return this.order;
    }

    private void saveOrder() {
        Window win = SwingUtilities.getWindowAncestor(this);
        ArrayList<String> errors = new ArrayList<String>();

        if (clientField.getSelectedIndex() == 0)
            errors.add("Выберите клиента");

        if (errors.size() > 0) {
            String errorMessage = "";
            for (String error : errors) {
                errorMessage += error+"\n";
            }

            JOptionPane.showMessageDialog(win,errorMessage,"Ошибка",JOptionPane.ERROR_MESSAGE);
        } else {
            order.setClient(new Client(((ComboItem)clientField.getSelectedItem()).getValue()));
            order.setStatus(new Library(((ComboItem)statusField.getSelectedItem()).getValue(),"orders_statuses"));
            order.setPaymentStatus(new Library(((ComboItem)paymentStatusField.getSelectedItem()).getValue(),"payment_statuses"));
            if (order.getId() == 0)
                order.setDate(System.currentTimeMillis());

            saved = true;
            win.dispose();
        }
    }

    private void cancelDialog() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null)
            win.dispose();
    }
}

