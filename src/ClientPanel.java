import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by User on 23.04.2017.
 */
public class ClientPanel extends JPanel {

    private Client client;

    private JTextField nameField = new JTextField("",30);
    private JTextField phoneField = new JTextField("",30);

    private JButton saveButton = new JButton("Сохранить");
    private JButton cancelButton = new JButton("Отмена");

    private boolean saved = false;

    public ClientPanel() {
        this.client = new Client();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,5);

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("ФИО*:"),c);
        c.gridx = 1;
        add(nameField,c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Телефон*:"),c);
        c.gridx = 1;
        add(phoneField,c);

        c.gridx = 0;
        c.gridy = 3;
        add(saveButton,c);
        c.gridy = 4;
        add(cancelButton,c);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveUser();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDialog();
            }
        });
    }

   public ClientPanel (Client cln) {
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
                saveUser();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDialog();
            }
        });
    }

    protected boolean isSaved() {
        return saved;
    }

    protected Client getClient() {
        return this.client;
    }

    private void saveUser() {
        Window win = SwingUtilities.getWindowAncestor(this);
        ArrayList<String> errors = new ArrayList<String>();

        if (nameField.getText().length() < 2)
            errors.add("Введите ФИО");
        if (phoneField.getText().length() < 2)
            errors.add("Введите телефон");

        if (errors.size() > 0) {
            String errorMessage = "";
            for (String error : errors) {
                errorMessage += error+"\n";
            }

            JOptionPane.showMessageDialog(win,errorMessage,"Ошибка",JOptionPane.ERROR_MESSAGE);
        } else {
            client.setName(nameField.getText());
            client.setPhone(phoneField.getText());
            if (client.getId() == 0)
                client.setRegistration(System.currentTimeMillis());

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

