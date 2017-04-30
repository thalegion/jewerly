import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Created by User on 16.04.2017.
 */
public class WorkersForm {

    private JFrame startFrame;
    private JTable listTable;
    private WorkerTableModel model;

    private JTextField searchField;
    private JTextField phoneSearchField;

    public WorkersForm() {

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        startFrame = new JFrame("Работники | Ювелирный магазин");
        startFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        startFrame.setLocationRelativeTo(null);

        startFrame.setSize(600,400);

        ResultSet modelSet = main.db.select("*","workers","","","");

        model = new WorkerTableModel(modelSet);
        model.addModelListener(new ModelUpdateListener() {
            @Override
            public void modelUpdated() {
                updateModel();
            }
        });
        main.db.closeStatementSet();

        listTable = new JTable(model);

        ButtonColumn deleteBtnColumn = new ButtonColumn(listTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                ((WorkerTableModel)table.getModel()).deleteValueAt(modelRow);
            }
        },2);

        JScrollPane scroll = new JScrollPane(listTable);
        startFrame.add(scroll,BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JPanel searchPanel = new JPanel();
        JTextField nameField = new JTextField("",15);
        JTextField phoneField = new JTextField("",15);

        searchField = new JTextField("",15);
        phoneSearchField = new JTextField("",15);
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
        phoneSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
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
                String name = nameField.getText();
                String phone = phoneField.getText();
                nameField.setText("");
                phoneField.setText("");
                if (name.length() > 0 && phone.length() > 0) {
                    Worker wrk = new Worker(name,phone);
                    model.insert(wrk);
                } else
                    JOptionPane.showMessageDialog(startFrame,"Заполните все поля.","Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.add(new JLabel("ФИО:"));
        controlPanel.add(nameField);
        controlPanel.add(new JLabel("Телефон:"));
        controlPanel.add(phoneField);
        controlPanel.add(addButton);

        searchPanel.add(new JLabel("Наименование:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Телефон:"));
        searchPanel.add(phoneSearchField);

        startFrame.add(searchPanel,BorderLayout.NORTH);
        startFrame.add(controlPanel,BorderLayout.SOUTH);

        startFrame.setVisible(true);
    }

    private void updateModel() {
        ResultSet modelSet = null;

        if (searchField.getText().length() > 0 || phoneSearchField.getText().length() > 0) {
            ArrayList<String> whereValuesList = new ArrayList<String>();
            String where = "";

            if (searchField.getText().length() > 0) {
                where += "name LIKE ? ";
                whereValuesList.add("%" + searchField.getText() + "%");
            }
            if (phoneSearchField.getText().length() > 0) {
                where += (where.length() > 0 ? "AND " : "") + "phone LIKE ?";
                whereValuesList.add("%" + phoneSearchField.getText() + "%");
            }

            String[] whereValues = new String[whereValuesList.size()];
            whereValuesList.toArray(whereValues);

            modelSet = main.db.select("*", "workers", where, whereValues, "", "");
        } else
            modelSet = main.db.select("*","workers","","","");

        model.update(modelSet);
        main.db.closeStatementSet();
    }
}

class WorkerTableModel extends AbstractTableModel {

    private ArrayList<Worker> libraries;
    private ModelUpdateListener listener;

    public WorkerTableModel(ResultSet rs) {
        libraries = new ArrayList<Worker>();

        try {
            while (rs.next()) {
                this.libraries.add(new Worker(rs));
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
        libraries.clear();

        try {
            while (rs.next()) {
                this.libraries.add(new Worker(rs));
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
                return String.class;
            case 1:
                return String.class;
            case 2:
                return JButton.class;
        }

        return String.class;
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "ФИО";
            case 1:
                return "Телефон";
            case 2:
                return "";
        }
        return "";
    }

    public int getRowCount() {
        if (libraries == null)
            return  0;
        return libraries.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Worker lib = libraries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return lib.getName();
            case 1:
                return lib.getPhone();
            case 2:
                return "Удалить";
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 2)
            return;
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите изменить значение?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Worker lib = libraries.get(rowIndex);
            if (columnIndex == 0)
                lib.setName(value.toString());
            else if (columnIndex == 1)
                lib.setPhone(value.toString());

            if (!lib.save())
                JOptionPane.showMessageDialog(null,"Невозможно обновить значение.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();
        }
    }

    public void insert (Worker lib) {
        if (lib.save())
            libraries.add(lib);
        else
            JOptionPane.showMessageDialog(null,"Невозможно добавить это значение.","Ошибка", JOptionPane.ERROR_MESSAGE);

        if (listener != null)
            listener.modelUpdated();
    }

    public void deleteValueAt (int rowIndex) {
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите удалить значение?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Worker lib = libraries.get(rowIndex);
            if (!lib.delete())
                JOptionPane.showMessageDialog(null,"Невозможно удалить значение.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();
        }
    }
}