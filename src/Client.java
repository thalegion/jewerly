import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by User on 23.04.2017.
 */
public class Client {

    private int id;
    private String name;
    private String phone;
    private Date registration;
    private ArrayList<Order> orders;

    public Client() {
        this.id = 0;
        this.name = "";
        this.phone = "";
        this.registration = new Date();
        this.orders = new ArrayList<Order>();
    }
    public Client(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.name = rs.getString("name");
            this.phone = rs.getString("phone");
            this.registration = new Date(rs.getLong("registration_date") * 1000);
            this.orders = new ArrayList<Order>();

            ResultSet ordersSet = main.db.select("*","orders","client_id = " + this.id,"date desc","");
            while (ordersSet.next()) {
                this.orders.add(new Order(ordersSet));
            }
            main.db.closeStatementSet(ordersSet);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
    public Date getRegistration() {return registration;}
    public int getOrdersCount(){
        return orders.size();
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setRegistration(long unix) {this.registration = new Date(unix);}

    public boolean save() {
        if (id == 0) {
            id = main.db.insert("clients", "name,phone,registration_date", new String[]{name,phone,String.valueOf(registration.getTime()/1000)});
            if (id > 0)
                return true;
        }
        else {
            if (main.db.update("clients", new String[]{"name","phone"}, new String[]{name,phone}, "id = ?", new String[]{String.valueOf(id)}) > 0)
                return true;
        }

        return false;
    }

    public boolean delete() {
        if (id == 0)
            return false;

        if (main.db.delete("clients","id = ?",new String[] {String.valueOf(id)}) > 0)
            return true;

        return false;
    }

}
