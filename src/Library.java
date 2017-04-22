import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by User on 18.04.2017.
 */
public class Library {

    private int id;
    private String name;

    public Library(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.name = rs.getString("name");
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
    public Library(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
