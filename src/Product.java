import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by User on 25.04.2017.
 */
public class Product {

    private int id;
    private double price;
    private Library status;
    private String description;
    private Library type;
    private Order order;
    private ArrayList<ProductMaterial> materials;
    private ArrayList<Worker> workers;

    public Product(int id) {
        ResultSet rs = null;
        try {
            rs = main.db.select("*","products","id = ?",new String[] {String.valueOf(id)},"","");
            rs.next();

            id = rs.getInt("id");
            price = rs.getDouble("price");
            status = new Library(rs.getInt("status"),"products_statuses");
            type = new Library(rs.getInt("type"),"products_types");
            description = rs.getString("description");
            order = new Order(rs.getInt("order_id"));

            materials = new ArrayList<ProductMaterial>();
            workers = new ArrayList<Worker>();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(rs);
        }
    }

    public Product(ResultSet rs) {
        try{
          id = rs.getInt("id");
          price = rs.getDouble("price");
          status = new Library(rs.getInt("status"),"products_statuses");
          type = new Library(rs.getInt("type"),"products_types");
          description = rs.getString("description");
          order = new Order(rs.getInt("order_id"));

          materials = new ArrayList<ProductMaterial>();
          workers = new ArrayList<Worker>();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
    public double getPrice() {
        return price;
    }
    public Library getStatus() {
        return status;
    }
    public Library getType() {
        return type;
    }
    public String getDescription() {
        return description;
    }
    public Order getOrder() {
        return order;
    }
    public ArrayList<ProductMaterial> getMaterials() {return materials;}

    public void setType(Library type) {
        this.type = type;
    }
    public void setStatus(Library status) {
        this.status = status;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setPrice(Double price) {
        this.price = price;
    }

    public void fillMaterials() {
        materials.clear();

        ResultSet fillSet = null;
        try {
            fillSet = main.db.select("*","products_materials","product_id = " + id,"","");
            while (fillSet.next())
                materials.add(new ProductMaterial(fillSet));
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }
    }

    public void fillWorkers() {
        workers.clear();

        ResultSet fillSet = null;
        try {
            fillSet = main.db.select("*","products_workers","product_id = " + id,"","");
            while (fillSet.next())
                workers.add(new Worker(fillSet.getInt("worker_id")));
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }
    }

    public boolean save() {
        if (id == 0) {
            id = main.db.insert("products", "type,price,status,description,order_id",
                    new String[]{String.valueOf(type.getId()),String.valueOf(price),String.valueOf(status.getId()),description,String.valueOf(order.getId())});
            if (id > 0)
                return true;
        }
        else {
            if (main.db.update("products", new String[]{"type","price","status","description"}, new String[]{String.valueOf(type.getId()),String.valueOf(price),String.valueOf(status.getId()),description}, "id = ?", new String[]{String.valueOf(id)}) > 0)
                return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete("products","id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }
}
