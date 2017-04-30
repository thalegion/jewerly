import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by User on 30.04.2017.
 */
public class ComboItem {
    private String key;
    private int value;

    public ComboItem() {
        this.key = "";
        this.value = 0;
    }

    public ComboItem(ResultSet rs, String key, String value) {
        try {
            this.key = rs.getString(key);
            this.value = rs.getInt(value);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return key;
    }

    public String getKey(){
        return key;
    }
    public int getValue() {
        return value;
    }
}
