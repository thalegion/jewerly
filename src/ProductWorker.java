import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by User on 01.05.2017.
 */
public class ProductWorker {

    private int id;
    private Product product;
    private Worker worker;

    public ProductWorker(Product p, Worker w) {
        product = p;
        worker = w;
    }

    public ProductWorker(ResultSet rs) {
        try {
            id = rs.getInt("id");
            product = new Product(rs.getInt("product_id"));
            worker = new Worker(rs.getInt("worker_id"));
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
    public Worker getWorker() {
        return worker;
    }

    public boolean save() {
        if (id == 0) {
            id = main.db.insert("products_workers", "product_id,worker_id",
                    new String[]{String.valueOf(product.getId()),String.valueOf(worker.getId())});
            if (id > 0)
                return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete("products_workers","id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }
}
