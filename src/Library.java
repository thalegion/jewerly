import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by User on 18.04.2017.
 */
public class Library {

    private int id;
    private String name;
    private String tableName;

    public  Library(int id, String tableName) {
        ResultSet rs = null;

        try{
            rs = main.db.select("*",tableName,"id = ?",new String[] {String.valueOf(id)},"","1");
            rs.next();

            this.id = rs.getInt("id");
            this.name = rs.getString("name");
            this.tableName = tableName;

            main.db.closeStatementSet(rs);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public Library(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.name = rs.getString("name");

            ResultSetMetaData rsmd = rs.getMetaData();
            this.tableName = rsmd.getTableName(1);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
    public Library(String name, String tableName) {
        this.name = name;
        this.tableName = tableName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean save() {
        if (id == 0) {
            id = main.db.insert(tableName, "name", new String[]{name});
            if (id > 0)
                return true;
        }
        else {
           if (main.db.update(tableName, new String[]{"name"}, new String[]{name}, "id = ?", new String[]{String.valueOf(id)}) > 0)
               return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete(tableName,"id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }

}
