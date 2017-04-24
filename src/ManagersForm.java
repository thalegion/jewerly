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
public class ManagersForm {

    private JFrame startFrame;
    private JTable listTable;
    private ManagerTableModel model;

    private JTextField searchField;
    private JCheckBox sudoCheckbox;

    public ManagersForm() {

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        startFrame = new JFrame("Менеджеры | Ювелирный магазин");
        startFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        startFrame.setLocationRelativeTo(null);

        startFrame.setSize(600,400);

        ResultSet modelSet = main.db.select("*","managers","id != " + main.activeManager.getId(),"","");

        model = new ManagerTableModel(modelSet);
        model.addModelListener(new ModelUpdateListener() {
            @Override
            public void modelUpdated() {
                updateModel();
            }
        });
        main.db.closeStatementSet();

        listTable = new JTable(model);

        ButtonColumn editBtnColumn = new ButtonColumn(listTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.valueOf( e.getActionCommand() );

                Manager mng = model.getManager(modelRow);
                JDialog managerDialog = new JDialog(startFrame,"Редактирование пользователя", Dialog.ModalityType.APPLICATION_MODAL);
                ManagerPanel managerPanel = new ManagerPanel(mng);

                managerDialog.setSize(600,400);
                managerDialog.getContentPane().add(managerPanel);
                managerDialog.setLocationRelativeTo(null);

                managerDialog.setVisible(true);

                if (managerPanel.isSaved()) {
                    mng = managerPanel.getManager();
                    if (!mng.save())
                        JOptionPane.showMessageDialog(startFrame,"Невозможно изменить данные пользователя.","Ошибка",JOptionPane.ERROR_MESSAGE);

                    updateModel();
                }
            }
        },2);
        ButtonColumn deleteBtnColumn = new ButtonColumn(listTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                ((ManagerTableModel)table.getModel()).deleteValueAt(modelRow);
            }
        },3);

        JScrollPane scroll = new JScrollPane(listTable);
        startFrame.add(scroll, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JPanel searchPanel = new JPanel();

        searchField = new JTextField("",30);

        sudoCheckbox = new JCheckBox();

        JButton addButton = new JButton("Добавить");


        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                updateModel();
            }
        });
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (searchField.getText().length() > 0)
                    updateModel();
            }
        });
        sudoCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                    if (!newManager.save())
                        JOptionPane.showMessageDialog(startFrame,"Невозможно добавить пользователя.","Ошибка",JOptionPane.ERROR_MESSAGE);
                    else
                        updateModel();
                }

            }
        });

        controlPanel.add(addButton);

        searchPanel.add(new JLabel("Логин:"));
        searchPanel.add(searchField);

        searchPanel.add(new JLabel("Только суперпользователи:"));
        searchPanel.add(sudoCheckbox);

        startFrame.add(searchPanel,BorderLayout.NORTH);
        startFrame.add(controlPanel,BorderLayout.SOUTH);

        startFrame.setVisible(true);
    }

    private void updateModel() {
        ResultSet modelSet = null;

        if (searchField.getText().length() > 0 || sudoCheckbox.isSelected()) {
            String where = "id != ? ";
            ArrayList<String> whereValuesList = new ArrayList<String>();

            whereValuesList.add(String.valueOf(main.activeManager.getId()));
            if (searchField.getText().length() > 0) {
                where += "AND login LIKE ? ";
                whereValuesList.add("%" + searchField.getText() + "%");
            }
            if (sudoCheckbox.isSelected()) {
                where += "AND sudo = ?";
                whereValuesList.add("1");
            }

            String[] whereValues = new String[whereValuesList.size()];
            whereValuesList.toArray(whereValues);

            modelSet = main.db.select("*", "managers", where, whereValues, "", "");
        } else
            modelSet = main.db.select("*","managers","id != " + main.activeManager.getId(),"","");

        model.update(modelSet);
        main.db.closeStatementSet();

    }
}

class ManagerTableModel extends AbstractTableModel {

    private ArrayList<Manager> managers;
    private ModelUpdateListener listener;

    public ManagerTableModel(ResultSet rs) {
        managers = new ArrayList<Manager>();

        try {
            while (rs.next()) {
                this.managers.add(new Manager(rs));
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
        managers.clear();

        try {
            while (rs.next()) {
                this.managers.add(new Manager(rs));
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
                return "Логин";
            case 1:
                return "Суперпользователь";
        }
        return "";
    }

    public int getRowCount() {
        if (managers == null)
            return  0;
        return managers.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Manager mng = managers.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return mng.getLogin();
            case 1:
                return mng.getSudo() ? "Да" : "Нет";
            case 2:
                return "Редактировать";
            case 3:
                return "Удалить";
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 2 || columnIndex == 3)
            return true;
        return false;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

    public Manager getManager(int rowIndex) {
        return managers.get(rowIndex);
    }

    public void deleteValueAt (int rowIndex) {
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите удалить пользователя?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Manager mng = managers.get(rowIndex);
            if (!mng.delete())
                JOptionPane.showMessageDialog(null,"Невозможно удалить значение.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();
        }
    }
}
