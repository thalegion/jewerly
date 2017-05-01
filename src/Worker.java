import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by User on 24.04.2017.
 */
public class Worker {

    private int id;
    private String name;
    private String phone;
    private ArrayList<ProductWorker> products;

    public Worker() {
        this.id = 0;
        this.name = "";
        this.phone = "";
    }

    public Worker(int id) {
        ResultSet rs = null;
        try {
            rs = main.db.select("*","workers","id = ?",new String[] {String.valueOf(id)},"","");
            rs.next();

            this.id = rs.getInt("id");
            name = rs.getString("name");
            phone = rs.getString("phone");

            products = new ArrayList<ProductWorker>();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(rs);
        }
    }

    public Worker(String name, String phone) {
        this.id = 0;
        this.name = name;
        this.phone = phone;

        products = new ArrayList<ProductWorker>();
    }

    public Worker(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.name = rs.getString("name");
            this.phone = rs.getString("phone");

            products = new ArrayList<ProductWorker>();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void fillProducts() {
        products.clear();

        ResultSet productsSet = null;
        try {
            productsSet = main.db.select("*", "products_workers", "worker_id = " + this.id, "id desc", "");
            while (productsSet.next()) {
                this.products.add(new ProductWorker(productsSet));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(productsSet);
        }

    }

    public int getId(){
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
    public int getProductsCount() {return products.size();}

    public void setName(String value) {
        name = value;
    }
    public void setPhone(String value) {
        phone = value;
    }

    public boolean save() {
        if (id == 0) {
            id = main.db.insert("workers", "name,phone", new String[]{name,phone});
            if (id > 0)
                return true;
        }
        else {
            if (main.db.update("workers", new String[]{"name","phone"}, new String[]{name,phone}, "id = ?", new String[]{String.valueOf(id)}) > 0)
                return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete("workers","id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }
}
