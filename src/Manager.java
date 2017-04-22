/**
 * Created by User on 16.04.2017.
 */
public class Manager {
    int id;
    String login;
    boolean sudo;

    public Manager(int id,String login, int sudo){
        this.id = id;
        this.login = login;
        this.sudo = sudo == 1 ? true : false;
    }
}
