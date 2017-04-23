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
public class ManagerPanel extends JPanel {

    private Manager manager;

    private JTextField loginField = new JTextField("",30);
    private JPasswordField passwordField = new JPasswordField("",30);
    private JPasswordField confirmPasswordField = new JPasswordField("",30);
    private JCheckBox sudoCheckbox = new JCheckBox();

    private JButton saveButton = new JButton("Сохранить");
    private JButton cancelButton = new JButton("Отмена");

    private boolean saved = false;

    public ManagerPanel() {
        this.manager = new Manager();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,5);

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Логин*:"),c);
        c.gridx = 1;
        add(loginField,c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Пароль*:"),c);
        c.gridx = 1;
        add(passwordField,c);

        c.gridx = 0;
        c.gridy = 2;
        add(new JLabel("Подтвердите пароль*:"),c);
        c.gridx = 1;
        add(confirmPasswordField,c);

        c.gridx = 0;
        c.gridy = 3;
        add(new JLabel("Супер пользователь:"),c);
        c.gridx = 1;
        add(sudoCheckbox,c);

        c.gridx = 0;
        c.gridy = 6;
        add(saveButton,c);
        c.gridy = 7;
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

    public ManagerPanel (Manager mgr) {
        manager = mgr;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,5);

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Логин*:"),c);
        c.gridx = 1;
        loginField.setText(manager.getLogin());
        add(loginField,c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Супер пользователь:"),c);
        c.gridx = 1;
        sudoCheckbox.setSelected(manager.getSudo());
        add(sudoCheckbox,c);

        c.gridx = 0;
        c.gridy = 3;
        add(new JLabel("Изменение пароля (опционально):"),c);

        c.gridx = 0;
        c.gridy = 4;
        add(new JLabel("Пароль:"),c);
        c.gridx = 1;
        add(passwordField,c);

        c.gridx = 0;
        c.gridy = 5;
        add(new JLabel("Подтвердите пароль:"),c);
        c.gridx = 1;
        add(confirmPasswordField,c);

        c.gridx = 0;
        c.gridy = 7;
        add(saveButton,c);
        c.gridy = 8;
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

    protected Manager getManager() {
        return this.manager;
    }

    private void saveUser() {
        Window win = SwingUtilities.getWindowAncestor(this);
        ArrayList<String> errors = new ArrayList<String>();

        if (loginField.getText().length() < 2)
            errors.add("Логин должен содержать хотя бы 2 символа;");
        else {
            int isSetLogin = 0;

            ResultSet isSetLoginRs = null;

            try {
                isSetLoginRs = main.db.select("count(*) as count", "managers",
                        "login = ?" + (manager.getId() != 0 ? " AND id != " + manager.getId() : ""),
                        new String[]{loginField.getText()}, "", "");
                isSetLoginRs.next();
                isSetLogin = isSetLoginRs.getInt("count");
            } catch (SQLException se) {
                se.printStackTrace();
            } finally {
                main.db.closeStatementSet();
            }

            if (isSetLogin > 0)
                errors.add("Такой логин уже существует;");
        }

        if (manager.getId() == 0 || (manager.getId() != 0 && passwordField.getPassword().length > 0)) {
            if (passwordField.getPassword().length < 6)
                errors.add("Пароль должен содержать не менее 6 символов;");
            else if (!String.valueOf(passwordField.getPassword()).equals(String.valueOf(confirmPasswordField.getPassword())))
                errors.add("Пароли должны совпадать;");
        }

        if (errors.size() > 0) {
            String errorMessage = "";
            for (String error : errors) {
                errorMessage += error+"\n";
            }

            JOptionPane.showMessageDialog(win,errorMessage,"Ошибка",JOptionPane.ERROR_MESSAGE);
        } else {
            manager.setLogin(loginField.getText());
            if (manager.getId() == 0 || (manager.getId() != 0 && passwordField.getPassword().length > 0))
                manager.changePassword(String.valueOf(passwordField.getPassword()));
            manager.setSudo(sudoCheckbox.isSelected());

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

