import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * Created by User on 16.04.2017.
 */
public class ListAddForm {

    private JFrame startFrame;
    private JTable listTable;

    public ListAddForm(String title, String tableName) {

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        startFrame = new JFrame(title + " | Ювелирный магазин");
        startFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        startFrame.setLocationRelativeTo(null);

        startFrame.setSize(600,400);

        LibraryTableModel model = new LibraryTableModel(tableName);

        listTable = new JTable(model);
        main.db.closeStatementSet();

        ButtonColumn deleteBtnColumn = new ButtonColumn(listTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                ((LibraryTableModel)table.getModel()).deleteValueAt(modelRow);
            }
        },2);

        JScrollPane scroll = new JScrollPane(listTable);
        startFrame.add(scroll,BorderLayout.CENTER);

        DefaultTableModel tmd = new DefaultTableModel();
        tmd.fireTableDataChanged();

        JPanel controlPanel = new JPanel();
        JPanel searchPanel = new JPanel();
        JLabel searchLabel = new JLabel("Поиск:");
        JTextField nameField = new JTextField("",30);
        JTextField searchField = new JTextField("",30);
        JButton addButton = new JButton("Добавить");


        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                model.search(searchField.getText());
            }
        });
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (searchField.getText().length() > 0)
                    model.search(searchField.getText());
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String libText = nameField.getText();
                if (libText.length() > 0) {
                    if (main.db.insert(tableName,"name",new String[]{libText}) > 0) {
                        nameField.setText("");
                        model.updateModel();
                    } else
                        JOptionPane.showMessageDialog(startFrame,"Невозможно добавить это значение.","Ошибка", JOptionPane.ERROR_MESSAGE);
                } else
                    JOptionPane.showMessageDialog(startFrame,"Значение должно быть задано.","Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.add(nameField);
        controlPanel.add(addButton);

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        startFrame.add(searchPanel,BorderLayout.NORTH);
        startFrame.add(controlPanel,BorderLayout.SOUTH);

        startFrame.setVisible(true);
    }
}

class LibraryTableModel extends AbstractTableModel {

    private String tableName;
    private ArrayList<Library> libraries;

    public LibraryTableModel(String tableName) {
        libraries = new ArrayList<Library>();
        this.tableName = tableName;

        ResultSet  tableValues = null;
        tableValues = main.db.select("*",tableName,"","","");

        updateModel(tableValues);
    }

    public void updateModel() {
        libraries.clear();

        ResultSet  tableValues = null;
        tableValues = main.db.select("*",tableName,"","","");

        updateModel(tableValues);
    }

    private void updateModel(ResultSet rs) {
        try {
            while (rs.next()) {
                this.libraries.add(new Library(rs));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet();
        }

        this.fireTableDataChanged();
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return int.class;
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
                return "Код";
            case 1:
                return "Значение";
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
        Library lib = libraries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return lib.getId();
            case 1:
                return lib.getName();
            case 2:
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
        if (columnIndex == 1) {
            if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите изменить значение?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                int updatedId = libraries.get(rowIndex).getId();
                if (main.db.update(tableName,new String[]{"name"},new String[]{value.toString()},"id = ?",new String[]{String.valueOf(updatedId)}) > 0) {
                    updateModel();
                } else
                    JOptionPane.showMessageDialog(null,"Невозможно обновить значение.","Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void deleteValueAt (int rowIndex) {
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите удалить значение?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int deletedId = libraries.get(rowIndex).getId();
            if (main.db.delete(tableName,"id = ?",new String[]{String.valueOf(deletedId)}) > 0) {
                updateModel();
            } else
                JOptionPane.showMessageDialog(null,"Невозможно удалить значение.","Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void search (String text) {
        libraries.clear();

        if (text.length() == 0)
            updateModel();
        else {
            text = "%" + text + "%";
            ResultSet tableValues = null;
            tableValues = main.db.select("*", tableName, "name LIKE ?", new String[]{text}, "", "");

            updateModel(tableValues);
        }
    }


}

/*

class EditButtonRenderer extends JButton implements TableCellRenderer {

    protected boolean editing;

    public EditButtonRenderer(){
        editing = false;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        setText((editing ? "Сохранить" : "Редактировать"));
        return this;
    }
}

class EditButtonEditor extends DefaultCellEditor {

    protected JButton button;

    private boolean isPushed;

    public EditButtonEditor(JTextField textField) {
        super(textField);

        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.print("Pressed Edit");
            }
        });
    }
}*/