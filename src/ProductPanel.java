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
    private JTextField materialPriceField = new JTextField("",15);

    private JTable materialsTable;
    private ProductMaterialTableModel materialsModel;
    private JComboBox materialField = new JComboBox();
    private JTextField materialCountField = new JTextField("",7);

    private JTable workersTable;
    private ProductWorkerTableModel workersModel;
    private JComboBox workerField = new JComboBox();

    private JButton saveButton = new JButton("Сохранить");
    private JButton cancelButton = new JButton("Отмена");

    private boolean saved = false;

    public ProductPanel(Product prd) {
        this.product = prd;


        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel tablesPanel = new JPanel(new BorderLayout());
        JPanel tablesMaterialPanel = new JPanel(new BorderLayout());
        JPanel tablesMaterialControlPanel = new JPanel();
        JPanel tablesWorkerPanel = new JPanel(new BorderLayout());
        JPanel tablesWorkerControlPanel = new JPanel();

        JPanel controlPanel = new JPanel();

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
               updateMaterialModel();
            }
        });

        materialsTable = new JTable(materialsModel);
        JScrollPane materialsScroll = new JScrollPane(materialsTable);
        new ButtonColumn(materialsTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                ((ProductMaterialTableModel) table.getModel()).deleteValueAt(modelRow);
            }
        }, 3);

        tablesMaterialControlPanel.add(new JLabel("Материал:"));
        tablesMaterialControlPanel.add(materialField);
        tablesMaterialControlPanel.add(new JLabel("Количество:"));
        tablesMaterialControlPanel.add(materialCountField);

        JButton tableMaterialAddBtn = new JButton("Добавить");
        tableMaterialAddBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (materialField.getSelectedIndex() > 0 && materialCountField.getText().length() > 0) {
                    Material m = new Material(((ComboItem)materialField.getSelectedItem()).getValue());
                    ProductMaterial pm = new ProductMaterial(product,m,Integer.valueOf(materialCountField.getText()));
                    materialCountField.setText("");
                    if (!pm.save())
                        JOptionPane.showMessageDialog(null,"Невозможно добавить этот материал. Возможно он уже добавлен.","Ошибка",JOptionPane.ERROR_MESSAGE);
                    updateMaterialModel();
                } else
                    JOptionPane.showMessageDialog(null,"Заполните все поля","Ошибка",JOptionPane.ERROR_MESSAGE);
            }
        });
        tablesMaterialControlPanel.add(tableMaterialAddBtn);

        materialCountField.getDocument().addDocumentListener(new IntDocumentListener(materialCountField));
        ResultSet materialsSet = null;
        materialField.addItem(new ComboItem());
        try {
            String mIds = product.getMaterialsSplitIds();
            materialsSet = main.db.select("*","materials",(mIds.length() > 0 ? "id NOT IN (" + mIds + ")" : ""),"","");
            while (materialsSet.next())
                materialField.addItem(new ComboItem(materialsSet,"name","id"));
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(materialsSet);
        }

        product.fillWorkers();

        workersModel = new ProductWorkerTableModel(product.getWorkers());
        workersModel.addModelListener(new ModelUpdateListener() {
            @Override
            public void modelUpdated() {
                updateWorkerModel();
            }
        });

        workersTable = new JTable(workersModel);
        JScrollPane workersScroll = new JScrollPane(workersTable);
        new ButtonColumn(workersTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                ((ProductWorkerTableModel) table.getModel()).deleteValueAt(modelRow);
            }
        }, 1);

        tablesWorkerControlPanel.add(new JLabel("Рабочий:"));
        tablesWorkerControlPanel.add(workerField);

        JButton tableWorkerAddBtn = new JButton("Добавить");
        tableWorkerAddBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (workerField.getSelectedIndex() > 0) {
                    Worker w = new Worker(((ComboItem)workerField.getSelectedItem()).getValue());
                    ProductWorker pw = new ProductWorker(product,w);
                    if (!pw.save())
                        JOptionPane.showMessageDialog(null,"Невозможно добавить работника. Возможно он уже добавлен.","Ошибка",JOptionPane.ERROR_MESSAGE);
                    updateWorkerModel();
                } else
                    JOptionPane.showMessageDialog(null,"Заполните все поля","Ошибка",JOptionPane.ERROR_MESSAGE);
            }
        });
        tablesWorkerControlPanel.add(tableWorkerAddBtn);

        ResultSet workersSet = null;
        workerField.addItem(new ComboItem());
        try {
            String mIds = product.getWorkersSplitIds();
            workersSet = main.db.select("*","workers",(mIds.length() > 0 ? "id NOT IN (" + mIds + ")" : ""),"","");
            while (workersSet.next())
                workerField.addItem(new ComboItem(workersSet,"name","id"));
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(workersSet);
        }


        c.gridx = 0;
        c.gridy = 0;
        fieldsPanel.add(new JLabel("Тип изделия:"),c);
        c.gridx = 1;
        fieldsPanel.add(typeField,c);

        c.gridx = 0;
        c.gridy = 1;
        fieldsPanel.add(new JLabel("Статус:"),c);
        c.gridx = 1;
        fieldsPanel.add(statusField,c);

        c.gridx = 0;
        c.gridy = 2;
        fieldsPanel.add(new JLabel("Описание:"),c);
        c.gridx = 1;
        fieldsPanel.add(descriptionField,c);

        c.gridx = 0;
        c.gridy = 3;
        fieldsPanel.add(new JLabel("Цена за материалы:"),c);
        c.gridx = 1;
        fieldsPanel.add(materialPriceField,c);
        materialPriceField.setEditable(false);

        Double materialTotalPrice = 0d;
        for (ProductMaterial pm : product.getMaterials())
            materialTotalPrice += pm.getPrice();
        materialPriceField.setText(String.valueOf(materialTotalPrice));

        c.gridx = 0;
        c.gridy = 4;
        fieldsPanel.add(new JLabel("Цена:"),c);
        c.gridx = 1;
        fieldsPanel.add(priceField,c);

        tablesMaterialPanel.add(materialsScroll,BorderLayout.NORTH);
        tablesMaterialPanel.add(tablesMaterialControlPanel,BorderLayout.SOUTH);
        tablesWorkerPanel.add(workersScroll,BorderLayout.NORTH);
        tablesWorkerPanel.add(tablesWorkerControlPanel,BorderLayout.SOUTH);
        tablesPanel.add(tablesMaterialPanel,BorderLayout.NORTH);
        tablesPanel.add(tablesWorkerPanel,BorderLayout.SOUTH);


        controlPanel.add(saveButton);
        controlPanel.add(cancelButton);

        add(fieldsPanel,BorderLayout.NORTH);
        add(tablesPanel,BorderLayout.CENTER);
        add(controlPanel,BorderLayout.SOUTH);


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

    protected void updateMaterialModel() {
        product.fillMaterials();
        materialsModel.update(product.getMaterials());

        ResultSet materialsSet = null;
        materialField.removeAllItems();
        materialField.addItem(new ComboItem());
        try {
            String mIds = product.getMaterialsSplitIds();
            materialsSet = main.db.select("*","materials",(mIds.length() > 0 ? "id NOT IN (" + mIds + ")" : ""),"","");
            while (materialsSet.next())
                materialField.addItem(new ComboItem(materialsSet,"name","id"));
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(materialsSet);
        }

        Double materialTotalPrice = 0d;
        for (ProductMaterial pm : product.getMaterials())
            materialTotalPrice += pm.getPrice();
        materialPriceField.setText(String.valueOf(materialTotalPrice));
    }

    protected void updateWorkerModel() {
        product.fillWorkers();
        workersModel.update(product.getWorkers());

        ResultSet workersSet = null;
        workerField.removeAllItems();
        workerField.addItem(new ComboItem());
        try {
            String mIds = product.getWorkersSplitIds();
            workersSet = main.db.select("*","workers",(mIds.length() > 0 ? "id NOT IN (" + mIds + ")" : ""),"","");
            while (workersSet.next())
                workerField.addItem(new ComboItem(workersSet,"name","id"));
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(workersSet);
        }
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

    public void update(ArrayList<ProductMaterial> pm) {
        products_materials = pm;

        fireTableDataChanged();
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return Integer.class;
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

class ProductWorkerTableModel extends AbstractTableModel {
    private ArrayList<ProductWorker> products_workers;
    private ModelUpdateListener listener;

    public ProductWorkerTableModel(ArrayList<ProductWorker> pm) {
        products_workers = pm;
    }

    public ProductWorkerTableModel(ResultSet rs) {
        products_workers = new ArrayList<ProductWorker>();

        try {
            while (rs.next()) {
                this.products_workers.add(new ProductWorker(rs));
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
        products_workers.clear();

        try {
            while (rs.next()) {
                this.products_workers.add(new ProductWorker(rs));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet();
        }

        fireTableDataChanged();
    }

    public void update(ArrayList<ProductWorker> pm) {
        products_workers = pm;

        fireTableDataChanged();
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return JButton.class;
        }

        return String.class;
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "ФИО работника";
        }
        return "";
    }

    public int getRowCount() {
        if (products_workers == null)
            return  0;
        return products_workers.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ProductWorker product_worker = products_workers.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return product_worker.getWorker().getName();
            case 1:
                return "Удалить";
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1)
            return true;
        return false;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

    public ProductWorker getProductWorker(int rowIndex) {
        return products_workers.get(rowIndex);
    }

    public void deleteValueAt (int rowIndex) {
        if (JOptionPane.showConfirmDialog(null,"Вы уверены, что хотите снять работника?","Вы уверены?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ProductWorker product = products_workers.get(rowIndex);
            if (!product.delete())
                JOptionPane.showMessageDialog(null,"Невозможно снять работника.","Ошибка", JOptionPane.ERROR_MESSAGE);

            if (listener != null)
                listener.modelUpdated();
        }
    }
}

