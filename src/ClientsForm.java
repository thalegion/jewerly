import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by User on 23.04.2017.
 */

public class ClientsForm {

    private JFrame startFrame;
    private JTable listTable;
    private ClientTableModel model;

    private JTextField nameSearchField;
    private JTextField phoneSearchField;

    public ClientsForm() {

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        startFrame = new JFrame("Клиенты | Ювелирный магазин");
        startFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        startFrame.setLocationRelativeTo(null);

        startFrame.setSize(600,400);

        ResultSet  modelSet = null;
        modelSet = main.db.select("*","clients","","id desc","");

        ClientTableModel model = new ClientTableModel(modelSet);
        model.addModelListener(new ModelUpdateListener() {
            @Override
            public void modelUpdated() {
                updateModel();
            }
        });

        listTable = new JTable(model);
        main.db.closeStatementSet();

        ButtonColumn editBtnColumn = new ButtonColumn(listTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*JTable table = (JTable)e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );

                //Manager mng = model.getClient(modelRow);
                JDialog managerDialog = new JDialog(startFrame,"Редактирование пользователя", Dialog.ModalityType.APPLICATION_MODAL);
                ManagerPanel managerPanel = new ManagerPanel(mng);

                managerDialog.setSize(600,400);
                managerDialog.getContentPane().add(managerPanel);
                managerDialog.setLocationRelativeTo(null);

                managerDialog.setVisible(true);

                if (managerPanel.isSaved()) {
                    mng = managerPanel.getManager();
                    if (main.db.update("managers",new String[] {"login","password","sudo"},new String[] {mng.getLogin(),mng.getPassword(),mng.getSudo() ? "1" : "0"},"id = ?",new String[] {String.valueOf(mng.getId())}) > 0)
                        model.updateModel();
                    else
                        JOptionPane.showMessageDialog(startFrame,"Невозможно изменить данные пользователя.","Ошибка",JOptionPane.ERROR_MESSAGE);

                }*/
            }
        },2);

        JScrollPane scroll = new JScrollPane(listTable);
        startFrame.add(scroll, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JPanel searchPanel = new JPanel();

        nameSearchField = new JTextField("",30);
        phoneSearchField = new JTextField("",30);

        JButton addButton = new JButton("Добавить");


        nameSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                updateModel();
            }
        });
        phoneSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                updateModel();
            }
        });
        nameSearchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (nameSearchField.getText().length() > 0)
                    updateModel();
            }
        });
        phoneSearchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (phoneSearchField.getText().length() > 0)
                    updateModel();
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JDialog managerDialog = new JDialog(startFrame,"Добавление пользователя", Dialog.ModalityType.APPLICATION_MODAL);
                ManagerPanel managerPanel = new ManagerPanel();

                managerDialog.setSize(600,400);
                managerDialog.getContentPane().add(managerPanel);
                managerDialog.setLocationRelativeTo(null);

                managerDialog.setVisible(true);

                if (managerPanel.isSaved()) {
                    Manager newManager = managerPanel.getManager();
                    if (main.db.insert("managers","login,password,sudo",new String[] {newManager.getLogin(),newManager.getPassword(),newManager.getSudo() ? "1" : "0"}) > 0)
                        model.updateModel();
                    else
                        JOptionPane.showMessageDialog(startFrame,"Невозможно добавить пользователя.","Ошибка",JOptionPane.ERROR_MESSAGE);

                }

            }
        });

        controlPanel.add(addButton);

        searchPanel.add(new JLabel("ФИО:"));
        searchPanel.add(nameSearchField);

        searchPanel.add(new JLabel("Телефон:"));
        searchPanel.add(phoneSearchField);

        startFrame.add(searchPanel,BorderLayout.NORTH);
        startFrame.add(controlPanel,BorderLayout.SOUTH);

        startFrame.setVisible(true);
    }

    private void updateModel() {
        ResultSet modelSet = null;

        if (nameSearchField.getText().length() > 0 || phoneSearchField.getText().length() > 0) {
            ArrayList<String> whereValuesList = new ArrayList<String>();
            String where = "";

            if (nameSearchField.getText().length() > 0) {
                where += "name LIKE ? ";
                whereValuesList.add("%" + nameSearchField.getText() + "%");
            }
            if (phoneSearchField.getText().length() > 0) {
                where += (where.length() > 0 ? "AND " : "") + "phone LIKE ?";
                whereValuesList.add("%" + phoneSearchField.getText() + "%");
            }

            String[] whereValues = new String[whereValuesList.size()];
            whereValuesList.toArray(whereValues);

            modelSet = main.db.select("*", "clients", where, whereValues, "id desc", "");
        } else
            modelSet = main.db.select("*","clients","","id desc","");

        model.update(modelSet);
        main.db.closeStatementSet();
    }
}


class ClientTableModel extends AbstractTableModel {

    private ArrayList<Client> clients;
    private ModelUpdateListener listener;


    public ClientTableModel(ResultSet rs) {
        clients = new ArrayList<Client>();

        try {
            while (rs.next()) {
                this.clients.add(new Client(rs));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet();
        }
    }

    public void addModelListener(ModelUpdateListener listener) {
        this.listener = listener;
    }

    public void update(ResultSet rs) {
        clients.clear();

        try {
            while (rs.next()) {
                this.clients.add(new Client(rs));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet();
        }

        fireTableDataChanged();
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
                return String.class;
            case 2:
                return int.class;
            case 3:
                return JButton.class;
        }

        return String.class;
    }

    public int getColumnCount() {
        return 4;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "ФИО";
            case 1:
                return "Телефон";
            case 2:
                return "Количество заказов";
        }
        return "";
    }

    public int getRowCount() {
        if (clients == null)
            return  0;
        return clients.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Client client = clients.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return client.getName();
            case 1:
                return client.getPhone();
            case 2:
                return client.getOrdersCount();
            case 3:
                return "Редактировать";
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 3)
            return true;
        return false;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

    public Client getClient(int rowIndex) {
        return clients.get(rowIndex);
    }

}
