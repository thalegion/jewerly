import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by User on 30.04.2017.
 */
public class ProductMaterial {

    private int id;
    private Product product;
    private Material material;
    private int count;

    public ProductMaterial(ResultSet rs) {
        try {
          id = rs.getInt("id");
          product = new Product(rs.getInt("product_id"));
          material = new Material(rs.getInt("material_id"));
          count = rs.getInt("count");
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
    public Product getProduct() {
        return product;
    }
    public Material getMaterial() {
        return material;
    }
    public int getCount() {
        return count;
    }
    public double getPrice() {
        return count * material.getPrice();
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean save() {
        if (id == 0) {
            id = main.db.insert("products_materials", "product_id,material_id,count",
                    new String[]{String.valueOf(product.getId()),String.valueOf(material.getId()),String.valueOf(count)});
            if (id > 0)
                return true;
        }
        else {
            if (main.db.update("products_materials", new String[]{"count"}, new String[]{String.valueOf(count)}, "id = ?", new String[]{String.valueOf(id)}) > 0)
                return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete("products_materials","id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }
}
