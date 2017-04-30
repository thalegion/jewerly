import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.scene.control.DatePicker;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 30.04.2017.
 */

public class ProductsForm {

    private JFrame startFrame;
    private JTable listTable;
    private ProductTableModel model;

    private JTextField idSearchField;
    private JTextField descriptionSearchField;
    private JComboBox statusSearchField;
    private JComboBox typeSearchField;

    public ProductsForm() {

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        startFrame = new JFrame("Изделия | Ювелирный магазин");
        startFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        startFrame.setSize(1200,400);
        startFrame.setLocationRelativeTo(null);

        ResultSet  modelSet = null;
        modelSet = main.db.select("*","products","","id desc","");

        model = new ProductTableModel(modelSet);
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
            int modelRow = Integer.valueOf( e.getActionCommand() );

            Product product = model.getProduct(modelRow);
            JDialog productDialog = new JDialog(startFrame,"Редактирование изделия", Dialog.ModalityType.APPLICATION_MODAL);
            ProductPanel productPanel = new ProductPanel(product);

            productDialog.setSize(600,400);
            productDialog.getContentPane().add(productPanel);
            productDialog.setLocationRelativeTo(null);

            productDialog.setVisible(true);

            if (productPanel.isSaved()) {
                product = productPanel.getProduct();
                if (!product.save())
                    JOptionPane.showMessageDialog(startFrame,"Невозможно изменить данные изделия.","Ошибка",JOptionPane.ERROR_MESSAGE);

                updateModel();
            }
            }
        },5);

        if (main.activeManager.getSudo()) {
            ButtonColumn deleteBtnColumn = new ButtonColumn(listTable, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JTable table = (JTable) e.getSource();
                    int modelRow = Integer.valueOf(e.getActionCommand());
                    ((ProductTableModel) table.getModel()).deleteValueAt(modelRow);
                }
            }, 6);
        }

        JScrollPane scroll = new JScrollPane(listTable);

        JPanel searchPanel = new JPanel();

        idSearchField = new JTextField("",5);
        idSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                updateModel();
            }
        });
        idSearchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (idSearchField.getText().length() > 0)
                    updateModel();
            }
        });

        descriptionSearchField = new JTextField("",15);
        descriptionSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                updateModel();
            }
        });
        descriptionSearchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (descriptionSearchField.getText().length() > 0)
                    updateModel();
            }
        });

        statusSearchField = new JComboBox();
        statusSearchField.addItem(new ComboItem());
        ResultSet statuses = null;
        statuses = main.db.select("*","products_statuses","","","");
        try {
            while (statuses.next()) {
                statusSearchField.addItem(new ComboItem(statuses,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(statuses);
        }
        statusSearchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateModel();
            }
        });

        typeSearchField = new JComboBox();
        typeSearchField.addItem(new ComboItem());
        statuses = null;
        statuses = main.db.select("*","products_types","","","");
        try {
            while (statuses.next()) {
                typeSearchField.addItem(new ComboItem(statuses,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(statuses);

        }
        typeSearchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateModel();
            }
        });

        searchPanel.add(new JLabel("№ заказа:"));
        searchPanel.add(idSearchField);

        searchPanel.add(new JLabel("Описание:"));
        searchPanel.add(descriptionSearchField);

        searchPanel.add(new JLabel("Тип заказа:"));
        searchPanel.add(typeSearchField);

        searchPanel.add(new JLabel("Статус заказа:"));
        searchPanel.add(statusSearchField);

        startFrame.add(searchPanel,BorderLayout.NORTH);
        startFrame.add(scroll, BorderLayout.CENTER);

        startFrame.setVisible(true);
    }

    private void updateModel() {
        ResultSet modelSet = null;

        if (idSearchField.getText().length() > 0 ||
                descriptionSearchField.getText().length() > 0 ||
                statusSearchField.getSelectedIndex() > 0 ||
                typeSearchField.getSelectedIndex() > 0) {
            ArrayList<String> whereValuesList = new ArrayList<String>();
            String where = "";
            Boolean orderJoin = false;

            if (idSearchField.getText().length() > 0) {
                where += "o.id = ? ";
                whereValuesList.add(idSearchField.getText());
                orderJoin = true;
            }
            if (descriptionSearchField.getText().length() > 0) {
                where += (where.length() > 0 ? "AND " : "") + "p.description LIKE ?";
                whereValuesList.add("%" + descriptionSearchField.getText() + "%");
            }

            if (statusSearchField.getSelectedIndex() > 0) {
                where += (where.length() > 0 ? "AND " : "") + "p.status = ? ";
                whereValuesList.add(String.valueOf(((ComboItem)statusSearchField.getSelectedItem()).getValue()));
            }

            if (typeSearchField.getSelectedIndex() > 0) {
                where += (where.length() > 0 ? "AND " : "") + "p.type = ? ";
                whereValuesList.add(String.valueOf(((ComboItem)typeSearchField.getSelectedItem()).getValue()));
            }

            String[] whereValues = new String[whereValuesList.size()];
            whereValuesList.toArray(whereValues);

            modelSet = main.db.query("SELECT p.* FROM products as p " +
                            (orderJoin ? "JOIN orders as o " :"") +
                            "WHERE " + where + "ORDER BY p.id desc LIMIT 50",
                    whereValues);
        } else
            modelSet = main.db.select("*","products","","id desc","");

        model.update(modelSet);
        main.db.closeStatementSet();
    }
}


class ProductTableModel extends AbstractTableModel {

    private ArrayList<Product> products;
    private ModelUpdateListener listener;


    public ProductTableModel(ResultSet rs) {
        products = new ArrayList<Product>();

        try {
            while (rs.next()) {
                this.products.add(new Product(rs));
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
        products.clear();

        try {
            while (rs.next()) {
                this.products.add(new Product(rs));
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
            case 2:
            case 3:
                return String.class;
            case 4:
                return Double.class;
            case 5:
            case 6:
                return JButton.class;
        }

        return String.class;
    }

    public int getColumnCount() {
        return main.activeManager.getSudo() ? 7 : 6;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "№ заказа";
            case 1:
                return "Описание";
            case 2:
                return "Тип изделия";
            case 3:
                return "Статус изделия";
            case 4:
                return "Цена";
        }
        return "";
    }

    public int getRowCount() {
        if (products == null)
            return  0;
        return products.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Product product = products.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return product.getOrder().getId();
            case 1:
                return product.getDescription();
            case 2:
                return product.getType().getName();
            case 3:
                return product.getStatus().getName();
            case 4:
                return product.getPrice();
            case 5:
                return "Редактировать";
            case 6:
                return "Удалить";
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 5 || columnIndex == 6)
            return true;
        return false;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

    public Product getProduct(int rowIndex) {
        return products.get(rowIndex);
    }

    public void deleteValueAt (int rowIndex) {
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите удалить изделие?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Product product = products.get(rowIndex);
            if (!product.delete())
                JOptionPane.showMessageDialog(null,"Невозможно удалить изделие.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();
        }
    }
}
