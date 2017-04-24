import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by User on 24.04.2017.
 */
public class Material {

    private int id;
    private String name;
    private Float price;

    public Material() {
        this.id = 0;
        this.name = "";
        this.price = 0f;
    }

    public Material(String name, Float price) {
        this.id = 0;
        this.name = name;
        this.price = price;
    }

    public Material(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.name = rs.getString("name");
            this.price = rs.getFloat("price");
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public int getId(){
        return id;
    }
    public String getName() {
        return name;
    }
    public Float getPrice() {
        return price;
    }

    public void setName(String value) {
        name = value;
    }
    public void setPrice(Float value) {
        price = value;
    }

    public boolean save() {
        if (id == 0) {
            id = main.db.insert("materials", "name,price", new String[]{name,String.valueOf(price)});
            if (id > 0)
                return true;
        }
        else {
            if (main.db.update("materials", new String[]{"name","price"}, new String[]{name,String.valueOf(price)}, "id = ?", new String[]{String.valueOf(id)}) > 0)
                return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete("materials","id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }
}
