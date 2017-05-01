import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by User on 23.04.2017.
 */
public class Order {

    private int id;
    private Date date;
    private Double price;
    private Library status;
    private Library payment_status;
    private Client client;
    private ArrayList<Product> products;

    public Order() {
        id = 0;
        date = new Date();
        price = 0d;
        products = new ArrayList<Product>();
    }

    public Order(int id) {
        ResultSet rs = null;

        try {
            rs = main.db.select("*","orders","id = ?",new String[] {String.valueOf(id)},"","1");
            rs.next();

            this.id = rs.getInt("id");
            date = new Date(rs.getLong("date") * 1000);
            price = rs.getDouble("price");
            status = new Library(rs.getInt("status"),"orders_statuses");
            payment_status = new Library(rs.getInt("payment_status"),"payment_statuses");
            client = new Client(rs.getInt("client_id"));
            products = new ArrayList<Product>();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(rs);
        }
    }

    public Order(ResultSet rs) {
        try{
            id = rs.getInt("id");
            date = new Date(rs.getLong("date") * 1000);
            price = rs.getDouble("price");
            status = new Library(rs.getInt("status"),"orders_statuses");
            payment_status = new Library(rs.getInt("payment_status"),"payment_statuses");
            client = new Client(rs.getInt("client_id"));
            products = new ArrayList<Product>();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public int getId(){
        return id;
    }

    public Client getClient() {
        return client;
    }
    public Date getDate() {
        return date;
    }
    public Library getStatus() {
        return status;
    }
    public Library getPaymentStatus() {
        return payment_status;
    }
    public Double getPrice() {return price;}
    public ArrayList<Product> getProducts() {return products;}

    public void setClient(Client client) {
        this.client = client;
    }
    public void setStatus(Library status) {
        this.status = status;
    }
    public void setPaymentStatus(Library status) {
        this.payment_status = status;
    }
    public void setDate(long unix) {this.date = new Date(unix);}
    public void setPrice(Double price) {this.price = price;}

    public void fillProducts() {
        products.clear();

        ResultSet productsSet = null;
        try{
            productsSet = main.db.select("*","products","order_id = " + this.id,"id desc","");
            while (productsSet.next()) {
                this.products.add(new Product(productsSet));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(productsSet);
        }
    }

    public boolean save() {
        if (id == 0) {
            id = main.db.insert("orders", "date,price,status,payment_status,client_id",
                    new String[]{String.valueOf(date.getTime()/1000),String.valueOf(price),String.valueOf(status.getId()),String.valueOf(payment_status.getId()),String.valueOf(client.getId())});
            if (id > 0)
                return true;
        }
        else {
            if (main.db.update("orders", new String[]{"price","status","payment_status"}, new String[]{String.valueOf(price),String.valueOf(status.getId()),String.valueOf(payment_status.getId())}, "id = ?", new String[]{String.valueOf(id)}) > 0)
                return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete("orders","id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }
}
