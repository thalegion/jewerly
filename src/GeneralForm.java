import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by User on 16.04.2017.
 */
public class GeneralForm {
    static void showForm() {

        JFrame startFrame = new JFrame("Главная | Ювелирный магазин");
        startFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        startFrame.setLocationRelativeTo(null);

        startFrame.setSize(600,400);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        Insets standardInset = new Insets(5,0,0,0);
        c.insets = standardInset;

        JButton clientsButton = new JButton("Клиенты");
        JButton workersButton = new JButton("Работники");
        JButton ordersButton = new JButton("Заказы");
        JButton productsButton = new JButton("Изделия");
        JButton materialsButton = new JButton("Материалы");

        mainPanel.add(clientsButton,c);
        mainPanel.add(workersButton,c);
        mainPanel.add(ordersButton,c);
        mainPanel.add(productsButton,c);
        mainPanel.add(materialsButton,c);

        if (main.activeManager.sudo) {
            JLabel sudoLabel = new JLabel("Рут функции:");
            JButton managersButton = new JButton("Менеджеры");

            JButton ordersStatusesButton = new JButton("Статусы заказов");
            ordersStatusesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new ListAddForm("Статусы заказов","orders_statuses");
                }
            });

            JButton productsStatusesButton = new JButton("Статусы изделий");
            productsStatusesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new ListAddForm("Статусы изделий", "products_statuses");
                }
            });

            JButton productsTypesButton = new JButton("Типы изделий");
            productsTypesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new ListAddForm("Типы изделия", "products_types");
                }
            });

            c.insets = new Insets(15,0,5,0);
            mainPanel.add(sudoLabel,c);

            c.insets = standardInset;
            mainPanel.add(managersButton,c);
            mainPanel.add(ordersStatusesButton,c);
            mainPanel.add(productsStatusesButton,c);
            mainPanel.add(productsTypesButton,c);
        }

        startFrame.add(mainPanel,BorderLayout.CENTER);
        startFrame.setVisible(true);
    }
}
