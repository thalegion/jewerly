import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by User on 16.04.2017.
 */
public class Manager {
    private int id;
    private String login;
    private String password;
    private boolean sudo;


    public Manager() {
        this.id = 0;
        this.login = "";
        this.password = "";
        this.sudo = false;
    }

    public Manager(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.login = rs.getString("login");
            this.password = rs.getString("password");
            this.sudo = rs.getInt("sudo") == 1 ? true : false;
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public Manager(String login, String password, int sudo){
        this.login = login;
        this.password = password;
        this.sudo = sudo == 1 ? true : false;
    }

    public int getId() {
        return this.id;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean getSudo() {
        return this.sudo;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setSudo (Boolean sudo) {
        this.sudo = sudo;
    }

    public  String changePassword(String password) {
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

        this.password = password;
        return password;
    }

    public boolean save() {
        if (id == 0) {
            id = main.db.insert("managers", "login,password,sudo", new String[]{login,password,sudo?"1":"0"});
            if (id > 0)
                return true;
        }
        else {
            if (main.db.update("managers", new String[]{"login","password","sudo"}, new String[]{login,password,sudo?"1":"0"}, "id = ?", new String[]{String.valueOf(id)}) > 0)
                return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete("managers","id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }
}
