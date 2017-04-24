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
public class MaterialsForm {

    private JFrame startFrame;
    private JTable listTable;
    private MaterialTableModel model;

    private JTextField searchField;
    private JTextField priceStartSearchField;
    private JTextField priceEndSearchField;

    public MaterialsForm() {

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        startFrame = new JFrame("Материалы | Ювелирный магазин");
        startFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        startFrame.setLocationRelativeTo(null);

        startFrame.setSize(600,400);

        ResultSet modelSet = main.db.select("*","materials","","","");

        model = new MaterialTableModel(modelSet);
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
                ((MaterialTableModel)table.getModel()).deleteValueAt(modelRow);
            }
        },3);

        JScrollPane scroll = new JScrollPane(listTable);
        startFrame.add(scroll,BorderLayout.CENTER);

        DecimalFormat priceFormatter = new DecimalFormat("#.##");
        priceFormatter.setGroupingUsed(false);
        DecimalFormatSymbols decimalDelimeter = new DecimalFormatSymbols();
        decimalDelimeter.setDecimalSeparator('.');
        priceFormatter.setDecimalFormatSymbols(decimalDelimeter);

        JPanel controlPanel = new JPanel();
        JPanel searchPanel = new JPanel();
        JTextField nameField = new JTextField("",15);
        JFormattedTextField priceField = new JFormattedTextField(priceFormatter);
        priceField.setColumns(15);

        searchField = new JTextField("",15);
        priceStartSearchField = new JTextField("",7);
        priceEndSearchField = new JTextField("",7);
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
        priceStartSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                updateModel();
            }
        });
        priceStartSearchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (priceStartSearchField.getText().length() > 0)
                    updateModel();
            }
        });
        priceEndSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                updateModel();
            }
        });
        priceEndSearchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (priceEndSearchField.getText().length() > 0)
                    updateModel();
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                Float price = priceField.getText().length() > 0 ? Float.valueOf(priceField.getText()) : 0f;
                nameField.setText("");
                priceField.setText("0");
                if (name.length() > 0 && price > 0f) {
                    Material mat = new Material(name,price);
                    model.insert(mat);
                } else
                    JOptionPane.showMessageDialog(startFrame,"Значение должно быть задано.","Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.add(new JLabel("Наименование:"));
        controlPanel.add(nameField);
        controlPanel.add(new JLabel("Цена:"));
        controlPanel.add(priceField);
        controlPanel.add(addButton);

        searchPanel.add(new JLabel("Наименование:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Цена от:"));
        searchPanel.add(priceStartSearchField);
        searchPanel.add(new JLabel("до:"));
        searchPanel.add(priceEndSearchField);

        startFrame.add(searchPanel,BorderLayout.NORTH);
        startFrame.add(controlPanel,BorderLayout.SOUTH);

        startFrame.setVisible(true);
    }

    private void updateModel() {
        ResultSet modelSet = null;

        if (searchField.getText().length() > 0 || priceStartSearchField.getText().length() > 0 || priceEndSearchField.getText().length() > 0) {
            ArrayList<String> whereValuesList = new ArrayList<String>();
            String where = "";

            if (searchField.getText().length() > 0) {
                where += "name LIKE ? ";
                whereValuesList.add("%" + searchField.getText() + "%");
            }
            if (priceStartSearchField.getText().length() > 0) {
                where += (where.length() > 0 ? "AND " : "") + "price >= ?";
                whereValuesList.add(priceStartSearchField.getText());
            }
            if (priceEndSearchField.getText().length() > 0) {
                where += (where.length() > 0 ? "AND " : "") + "price <= ?";
                whereValuesList.add(priceEndSearchField.getText());
            }

            String[] whereValues = new String[whereValuesList.size()];
            whereValuesList.toArray(whereValues);

            modelSet = main.db.select("*", "materials", where, whereValues, "", "");
        } else
            modelSet = main.db.select("*","materials","","","");

        model.update(modelSet);
        main.db.closeStatementSet();
    }
}

class MaterialTableModel extends AbstractTableModel {

    private ArrayList<Material> libraries;
    private ModelUpdateListener listener;

    public MaterialTableModel(ResultSet rs) {
        libraries = new ArrayList<Material>();

        try {
            while (rs.next()) {
                this.libraries.add(new Material(rs));
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
                this.libraries.add(new Material(rs));
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
                return int.class;
            case 1:
                return String.class;
            case 2:
                return Float.class;
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
                return "Код";
            case 1:
                return "Название";
            case 2:
                return "Цена за грамм";
            case 3:
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
        Material lib = libraries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return lib.getId();
            case 1:
                return lib.getName();
            case 2:
                return lib.getPrice();
            case 3:
                return "Удалить";
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return false;
        else
            return true;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 3)
            return;
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите изменить значение?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Material lib = libraries.get(rowIndex);
            if (columnIndex == 1)
                lib.setName(value.toString());
            else if (columnIndex == 2)
                lib.setPrice(Float.valueOf(value.toString()));

            if (!lib.save())
                JOptionPane.showMessageDialog(null,"Невозможно обновить значение.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();
        }
    }

    public void insert (Material lib) {
        if (lib.save())
            libraries.add(lib);
        else
            JOptionPane.showMessageDialog(null,"Невозможно добавить это значение.","Ошибка", JOptionPane.ERROR_MESSAGE);

        if (listener != null)
            listener.modelUpdated();
    }

    public void deleteValueAt (int rowIndex) {
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите удалить значение?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Material lib = libraries.get(rowIndex);
            if (!lib.delete())
                JOptionPane.showMessageDialog(null,"Невозможно удалить значение.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();
        }
    }
}