import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Created by User on 16.04.2017.
 */
public class AuthForm {
    static void showForm() {

        JFrame startFrame = new JFrame("Вход | Ювелирный магазин");
        startFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        startFrame.setSize(600,400);
        startFrame.setLocationRelativeTo(null);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;

        JLabel errorFieldLabel = new JLabel();
        errorFieldLabel.setForeground(Color.RED);
        JLabel loginFieldLabel = new JLabel("Логин:");
        JTextField loginField = new JTextField("",10);
        JLabel passwordFieldLabel = new JLabel("Пароль:");
        JPasswordField passwordField = new JPasswordField("",10);
        JButton submitButton = new JButton("Вход");

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResultSet loggedSet = null;

                errorFieldLabel.setText("");
                String login = loginField.getText();
                String password = String.valueOf(passwordField.getPassword());

                if ((login.length() > 0) && (password.length() > 0)) {
                    try {
                        try {
                            MessageDigest mg = MessageDigest.getInstance("MD5");
                            mg.update(password.getBytes());

                            byte byteData[] = mg.digest();

                            //convert the byte to hex format method 1
                            StringBuffer sb = new StringBuffer();
                            for (int i = 0; i < byteData.length; i++) {
                                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
                            }
                            password = sb.toString();
                        } catch (NoSuchAlgorithmException ex){
                            ex.printStackTrace();
                        }

                        loggedSet = main.db.select("*","managers","login = ? AND password = ?",new String[]{login,password},"","1");


                        while (loggedSet.next()) {
                            main.activeManager = new Manager(loggedSet);

                            startFrame.setVisible(false);
                            startFrame.dispose();

                            GeneralForm.showForm();
                            break;
                        }
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    } finally {
                        main.db.closeStatementSet();
                    }

                }

                errorFieldLabel.setText("Введены неверные логин и\\или пароль.");
            }
        });


        loginPanel.add(errorFieldLabel,c);
        loginPanel.add(loginFieldLabel,c);
        loginPanel.add(loginField,c);
        loginPanel.add(passwordFieldLabel,c);
        loginPanel.add(passwordField,c);
        loginPanel.add(submitButton,c);


        startFrame.add(loginPanel,BorderLayout.CENTER);
        startFrame.setVisible(true);

    }
}
