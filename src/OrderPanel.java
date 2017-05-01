import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by User on 30.04.2017.
 */
public class OrderPanel extends JPanel {

    private Order order;

    private JComboBox clientField = new JComboBox();
    private JComboBox statusField = new JComboBox();
    private JComboBox paymentStatusField = new JComboBox();

    private JTextField priceField = new JTextField("",15);
    private JTextField productPriceField = new JTextField("",15);

    private JTable productsTable;
    private ProductTableModel productsModel;

    private JButton saveButton = new JButton("Сохранить");
    private JButton cancelButton = new JButton("Отмена");

    private boolean saved = false;

    public OrderPanel() {
        this.order = new Order();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,5);

        clientField.addItem(new ComboItem());
        ResultSet fillSet = null;
        try {
            fillSet = main.db.select("*","clients","","name ASC","");
            while (fillSet.next()) {
                clientField.addItem(new ComboItem(fillSet,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        fillSet = null;
        try {
            fillSet = main.db.select("*","orders_statuses","","name ASC","");
            while (fillSet.next()) {
                statusField.addItem(new ComboItem(fillSet,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        fillSet = null;
        try {
            fillSet = main.db.select("*","payment_statuses","","name ASC","");
            while (fillSet.next()) {
                paymentStatusField.addItem(new ComboItem(fillSet,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Клиент*:"),c);
        c.gridx = 1;
        add(clientField,c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Статус:"),c);
        c.gridx = 1;
        add(statusField,c);

        c.gridx = 0;
        c.gridy = 2;
        add(new JLabel("Статус оплаты:"),c);
        c.gridx = 1;
        add(paymentStatusField,c);

        c.gridx = 0;
        c.gridy = 3;
        add(saveButton,c);
        c.gridy = 4;
        add(cancelButton,c);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveOrder();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDialog();
            }
        });
    }

    public OrderPanel(Client cln) {
        this.order = new Order();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,5);

        clientField.addItem(new ComboItem());
        ResultSet fillSet = null;
        try {
            fillSet = main.db.select("*","clients","","name ASC","");
            while (fillSet.next()) {
                ComboItem ci = new ComboItem(fillSet,"name","id");
                clientField.addItem(ci);

                if (cln.getId() == ci.getValue())
                    clientField.setSelectedItem(ci);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        fillSet = null;
        try {
            fillSet = main.db.select("*","orders_statuses","","name ASC","");
            while (fillSet.next()) {
                statusField.addItem(new ComboItem(fillSet,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        fillSet = null;
        try {
            fillSet = main.db.select("*","payment_statuses","","name ASC","");
            while (fillSet.next()) {
                paymentStatusField.addItem(new ComboItem(fillSet,"name","id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            main.db.closeStatementSet(fillSet);
        }

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Клиент*:"),c);
        c.gridx = 1;
        add(clientField,c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Статус:"),c);
        c.gridx = 1;
        add(statusField,c);

        c.gridx = 0;
        c.gridy = 2;
        add(new JLabel("Статус оплаты:"),c);
        c.gridx = 1;
        add(paymentStatusField,c);

        c.gridx = 0;
        c.gridy = 3;
        add(saveButton,c);
        c.gridy = 4;
        add(cancelButton,c);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveOrder();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDialog();
            }
        });
    }

   public OrderPanel (Order ord) {
       order = ord;

       JPanel fieldsPanel = new JPanel(new GridBagLayout());
       GridBagConstraints c = new GridBagConstraints();

       JPanel tablePanel = new JPanel(new BorderLayout());


       c.insets = new Insets(5,5,5,5);

       clientField.addItem(order.getClient().getName());
       clientField.setEditable(false);

       ResultSet fillSet = null;
       try {
           fillSet = main.db.select("*","orders_statuses","","name ASC","");
           while (fillSet.next()) {
               ComboItem ci = new ComboItem(fillSet,"name","id");
               statusField.addItem(ci);

               if (order.getStatus().getId() == ci.getValue())
                   statusField.setSelectedItem(ci);
           }
       } catch (SQLException se) {
           se.printStackTrace();
       } finally {
           main.db.closeStatementSet(fillSet);
       }

       fillSet = null;
       try {
           fillSet = main.db.select("*","payment_statuses","","name ASC","");
           while (fillSet.next()) {
               ComboItem ci = new ComboItem(fillSet,"name","id");
               paymentStatusField.addItem(ci);

               if (order.getPaymentStatus().getId() == ci.getValue())
                   paymentStatusField.setSelectedItem(ci);
           }
       } catch (SQLException se) {
           se.printStackTrace();
       } finally {
           main.db.closeStatementSet(fillSet);
       }

       priceField.getDocument().addDocumentListener(new PriceDocumentListener(priceField));
       priceField.setText(String.valueOf(order.getPrice()));

       c.gridx = 0;
       c.gridy = 0;
       fieldsPanel.add(new JLabel("Клиент*:"),c);
       c.gridx = 1;
       fieldsPanel.add(clientField,c);

       c.gridx = 0;
       c.gridy = 1;
       fieldsPanel.add(new JLabel("Статус:"),c);
       c.gridx = 1;
       fieldsPanel.add(statusField,c);

       c.gridx = 0;
       c.gridy = 2;
       fieldsPanel.add(new JLabel("Статус оплаты:"),c);
       c.gridx = 1;
       fieldsPanel.add(paymentStatusField,c);

       c.gridx = 0;
       c.gridy = 3;
       fieldsPanel.add(new JLabel("Цена за изделия:"),c);
       c.gridx = 1;
       fieldsPanel.add(productPriceField,c);

       order.fillProducts();

       productPriceField.setEditable(false);
       Double productTotalPrice = 0d;
       for (Product p : order.getProducts())
           productTotalPrice += p.getPrice();
       productPriceField.setText(String.valueOf(productTotalPrice));

       c.gridx = 0;
       c.gridy = 4;
       fieldsPanel.add(new JLabel("Цена:"),c);
       c.gridx = 1;
       fieldsPanel.add(priceField,c);

       c.gridx = 0;
       c.gridy = 5;
       fieldsPanel.add(saveButton,c);
       c.gridy = 6;
       fieldsPanel.add(cancelButton,c);

       productsModel = new ProductTableModel(order.getProducts());
       productsModel.addModelListener(new ModelUpdateListener() {
           @Override
           public void modelUpdated() {
               updateModel();
           }
       });

       productsTable = new JTable(productsModel);
       JScrollPane productsScroll = new JScrollPane(productsTable);

       JButton addProduct = new JButton("Добавить");
       addProduct.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
                Product product = new Product(order);
                product.save();
                if (!editProduct(product))
                    product.delete();

                updateModel();
           }
       });

       new ButtonColumn(productsTable, new AbstractAction() {
           @Override
           public void actionPerformed(ActionEvent e) {
           int modelRow = Integer.valueOf( e.getActionCommand() );

           Product product = productsModel.getProduct(modelRow);
           editProduct(product);

           }
       },5);

       if (main.activeManager.getSudo()) {
           new ButtonColumn(productsTable, new AbstractAction() {
               @Override
               public void actionPerformed(ActionEvent e) {
                   JTable table = (JTable) e.getSource();
                   int modelRow = Integer.valueOf(e.getActionCommand());
                   ((ProductTableModel) table.getModel()).deleteValueAt(modelRow);
               }
           }, 6);
       }


       saveButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               saveOrder();
           }
       });
       cancelButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               cancelDialog();
           }
       });

       tablePanel.add(productsScroll,BorderLayout.NORTH);
       tablePanel.add(addProduct,BorderLayout.SOUTH);

       add(fieldsPanel,BorderLayout.NORTH);
       add(tablePanel,BorderLayout.CENTER);
    }

    protected boolean editProduct(Product p) {
        Window win = SwingUtilities.getWindowAncestor(this);
        JDialog productDialog = new JDialog(win,"Редактирование изделия", Dialog.ModalityType.APPLICATION_MODAL);
        ProductPanel productPanel = new ProductPanel(p);

        productDialog.setSize(600,400);
        productDialog.getContentPane().add(productPanel);
        productDialog.pack();
        productDialog.setLocationRelativeTo(null);

        productDialog.setVisible(true);

        if (productPanel.isSaved()) {
            p = productPanel.getProduct();
            Boolean res = p.save();
            if (!res)
                JOptionPane.showMessageDialog(win,"Невозможно изменить данные изделия.","Ошибка",JOptionPane.ERROR_MESSAGE);

            updateModel();

            return res;
        }

        return false;
    }

    protected void updateModel() {
        order.fillProducts();
        productsModel.update(order.getProducts());

        Double productTotalPrice = 0d;
        for (Product p : order.getProducts())
            productTotalPrice += p.getPrice();
        productPriceField.setText(String.valueOf(productTotalPrice));
    }

    protected boolean isSaved() {
        return saved;
    }

    protected Order getOrder() {
        return this.order;
    }

    private void saveOrder() {
        Window win = SwingUtilities.getWindowAncestor(this);
        ArrayList<String> errors = new ArrayList<String>();

        if (order.getId() == 0) {
            if (clientField.getSelectedIndex() == 0)
                errors.add("Выберите клиента");
        }

        if (errors.size() > 0) {
            String errorMessage = "";
            for (String error : errors) {
                errorMessage += error+"\n";
            }

            JOptionPane.showMessageDialog(win,errorMessage,"Ошибка",JOptionPane.ERROR_MESSAGE);
        } else {
            order.setStatus(new Library(((ComboItem)statusField.getSelectedItem()).getValue(),"orders_statuses"));
            order.setPaymentStatus(new Library(((ComboItem)paymentStatusField.getSelectedItem()).getValue(),"payment_statuses"));
            if (order.getId() == 0) {
                order.setDate(System.currentTimeMillis());
                order.setClient(new Client(((ComboItem)clientField.getSelectedItem()).getValue()));
            } else
                order.setPrice(priceField.getText().length() > 0 ? Double.valueOf(priceField.getText()) : 0);

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

