import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by User on 30.04.2017.
 */
public class ProductPanel extends JPanel {

    private Product product;

    private JComboBox typeField = new JComboBox();
    private JComboBox statusField = new JComboBox();
    private JTextArea descriptionField = new JTextArea(5,15);
    private JTextField priceField = new JTextField("",15);

    private JTable materialsTable;
    private ProductMaterialTableModel materialsModel;

    private JButton saveButton = new JButton("Сохранить");
    private JButton cancelButton = new JButton("Отмена");

    private boolean saved = false;

    public ProductPanel(Product prd) {
        this.product = prd;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,5);

        priceField.getDocument().addDocumentListener(new PriceDocumentListener(priceField));
        priceField.setText(String.valueOf(product.getPrice()));

        descriptionField.setText(product.getDescription());

        ResultSet fillSet = null;
        try {
            fillSet = main.db.select("*","products_types","","","");
            while (fillSet.next()) {
                ComboItem ci = new ComboItem(fillSet,"name","id");
                typeField.addItem(ci);

                if (product.getType().getId() == ci.getValue())
                    typeField.setSelectedItem(ci);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        fillSet = null;
        try {
            fillSet = main.db.select("*","products_statuses","","name ASC","");
            while (fillSet.next()) {
                ComboItem ci = new ComboItem(fillSet,"name","id");
                statusField.addItem(ci);

                if (product.getStatus().getId() == ci.getValue())
                    statusField.setSelectedItem(ci);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        product.fillMaterials();

        materialsModel = new ProductMaterialTableModel(product.getMaterials());
        materialsModel.addModelListener(new ModelUpdateListener() {
            @Override
            public void modelUpdated() {
               // updateMaterialModel();
            }
        });

        materialsTable = new JTable(materialsModel);
        materialsTable.setPreferredSize(new Dimension(300,200));
        materialsTable.setMinimumSize(new Dimension(300,200));
        JScrollPane materialsScroll = new JScrollPane(materialsTable);

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Тип изделия:"),c);
        c.gridx = 1;
        add(typeField,c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Статус:"),c);
        c.gridx = 1;
        add(statusField,c);

        c.gridx = 0;
        c.gridy = 2;
        add(new JLabel("Описание:"),c);
        c.gridx = 1;
        add(descriptionField,c);

        c.gridx = 0;
        c.gridy = 3;
        add(new JLabel("Цена:"),c);
        c.gridx = 1;
        add(priceField,c);

        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.gridheight = 2;
        c.gridx = 0;
        c.gridy = 4;
        add(materialsScroll,c);

        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 6;
        add(saveButton,c);
        c.gridy = 7;
        add(cancelButton,c);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProduct();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDialog();
            }
        });
    }

    protected boolean isSaved() {
        return saved;
    }

    protected Product getProduct() {
        return this.product;
    }

    private void saveProduct() {
        Window win = SwingUtilities.getWindowAncestor(this);
        ArrayList<String> errors = new ArrayList<String>();

        if (errors.size() > 0) {
            String errorMessage = "";
            for (String error : errors) {
                errorMessage += error+"\n";
            }

            JOptionPane.showMessageDialog(win,errorMessage,"Ошибка",JOptionPane.ERROR_MESSAGE);
        } else {
            product.setType(new Library(((ComboItem)typeField.getSelectedItem()).getValue(),"products_types"));
            product.setStatus(new Library(((ComboItem)statusField.getSelectedItem()).getValue(),"products_statuses"));
            product.setDescription(descriptionField.getText());
            product.setPrice(priceField.getText().length() > 0 ? Double.valueOf(priceField.getText()) : 0d);

            saved = true;
            win.dispose();
        }
    }

    private void cancelDialog() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null)
            win.dispose();
    }
}

class ProductMaterialTableModel extends AbstractTableModel {
    private ArrayList<ProductMaterial> products_materials;
    private ModelUpdateListener listener;

    public ProductMaterialTableModel(ArrayList<ProductMaterial> pm) {
        products_materials = pm;
    }

    public ProductMaterialTableModel(ResultSet rs) {
        products_materials = new ArrayList<ProductMaterial>();

        try {
            while (rs.next()) {
                this.products_materials.add(new ProductMaterial(rs));
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
        products_materials.clear();

        try {
            while (rs.next()) {
                this.products_materials.add(new ProductMaterial(rs));
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
                return int.class;
            case 2:
                return Double.class;
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
                return "Материал";
            case 1:
                return "Количество, грамм";
            case 2:
                return "Цена";
        }
        return "";
    }

    public int getRowCount() {
        if (products_materials == null)
            return  0;
        return products_materials.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ProductMaterial product_material = products_materials.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return product_material.getMaterial().getName();
            case 1:
                return product_material.getCount();
            case 2:
                return product_material.getPrice();
            case 3:
                return "Удалить";
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1 || columnIndex == 3)
            return true;
        return false;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            ProductMaterial pm = products_materials.get(rowIndex);
            pm.setCount((int)value);

            if (!pm.save())
                JOptionPane.showMessageDialog(null,"Невозможно изменить значение.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();

        }

    }

    public ProductMaterial getProductMaterial(int rowIndex) {
        return products_materials.get(rowIndex);
    }

    public void deleteValueAt (int rowIndex) {
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите удалить материал?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ProductMaterial product = products_materials.get(rowIndex);
            if (!product.delete())
                JOptionPane.showMessageDialog(null,"Невозможно удалить материал.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();
        }
    }
}

