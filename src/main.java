import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Locale;

/**
 * Created by User on 16.04.2017.
 */
public class main {
    private static final String db_url = "jdbc:mysql://localhost:3306/jewerly?useUnicode=true&characterEncoding=UTF-8";
    private static final String db_user = "root";
    private static final String db_password = "123";

    public static DatabaseController db;

    public static Manager activeManager;

    public static void main(String args[]){
        db = new DatabaseController(db_url,db_user,db_password);

        AuthForm.showForm();
    }
}
