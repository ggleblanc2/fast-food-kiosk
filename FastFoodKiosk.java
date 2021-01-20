package com.ggl.testing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class FastFoodKiosk implements Runnable {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new FastFoodKiosk());
	}
	
	private DefaultListModel<String> orderListModel;
	private DefaultListModel<String> paymentListModel;
	
	private Inventory inventory;
	
	private JTextField changeField;
	private JTextField paymentField;
	private JTextField totalField;
	
	private PaymentListener paymentListener;
	
	private Order order;
	
	public FastFoodKiosk() {
		this.inventory = new Inventory();
		this.order = new Order();
		this.orderListModel = new DefaultListModel<>();
		this.paymentListModel = new DefaultListModel<>();
		this.paymentListener = new PaymentListener();
	}

	@Override
	public void run() {
		JFrame frame = new JFrame("Fast Food Kiosk");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.add(createKisokPanel(), BorderLayout.CENTER);
		frame.add(createControlPanel(), BorderLayout.AFTER_LINE_ENDS);
		
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		System.out.println(frame.getSize());
	}
	
	private JPanel createKisokPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 4, 10, 10));
		panel.setBackground(Color.DARK_GRAY);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		OrderListener listener = new OrderListener();
		Font font = panel.getFont().deriveFont(16f);
		
		int index = 0;
		for (Item item : inventory.getItems()) {
			JPanel innerPanel = new JPanel(new BorderLayout());
			innerPanel.setBackground(Color.WHITE);
			
			Image foodImage = item.getImage();
			JButton imageButton = new JButton();
			imageButton.addActionListener(listener);
			imageButton.setActionCommand(Integer.toString(index));
			imageButton.setBackground(Color.WHITE);
			imageButton.setHorizontalAlignment(JLabel.CENTER);
			imageButton.setIcon(new ImageIcon(foodImage));
			innerPanel.add(imageButton, BorderLayout.CENTER);
			
			JPanel textButtonPanel = new JPanel(new BorderLayout());
			
			JPanel textPanel = new JPanel(new BorderLayout());
			textPanel.setBackground(Color.WHITE);
			
			JLabel nameLabel = new JLabel(item.getName());
			nameLabel.setFont(font);
			nameLabel.setHorizontalAlignment(JLabel.CENTER);
			textPanel.add(nameLabel, BorderLayout.BEFORE_FIRST_LINE);
			
			String price = "$" + String.format("%.2f", (double) item.getPrice() * 0.01d);
			JLabel priceLabel = new JLabel(price);
			priceLabel.setFont(font);
			priceLabel.setHorizontalAlignment(JLabel.CENTER);
			textPanel.add(priceLabel, BorderLayout.AFTER_LAST_LINE);
			
			JPanel buttonPanel = new JPanel(new FlowLayout());
			buttonPanel.setBackground(Color.WHITE);
			
			JButton removeButton = new JButton("Remove Item");
			removeButton.addActionListener(listener);
			removeButton.setActionCommand(Integer.toString(index++));
			removeButton.setFont(font);
			buttonPanel.add(removeButton);
			
			textButtonPanel.add(textPanel, BorderLayout.BEFORE_FIRST_LINE);
			textButtonPanel.add(buttonPanel, BorderLayout.AFTER_LAST_LINE);
			innerPanel.add(textButtonPanel, BorderLayout.AFTER_LAST_LINE);
			
			panel.add(innerPanel);
		}
		
		return panel;
	}
	
	private JPanel createControlPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(createOrderPanel(), BorderLayout.BEFORE_FIRST_LINE);
		panel.add(createPaymentPanel(), BorderLayout.AFTER_LAST_LINE);
		
		return panel;
	}
	
	private void updateOrderPanel() {
		orderListModel.removeAllElements();
		
		int subTotal = 0;
		for (Item item : order.getItems()) {
			subTotal += item.getPrice();
			orderListModel.addElement(createLine(item.getName(), 
					item.getPrice()));
		}
		
		double total = subTotal * 0.01d;
		double tax = total * 0.09d;
		total += tax;
		order.setOrderTotal(total);
		totalField.setText(String.format("%#.2f", total)); 
		
		orderListModel.addElement(createRepeatingLine("----------", 30));
		orderListModel.addElement(createLine("Subtotal", subTotal));
		orderListModel.addElement(createLine("9% Tax", tax));
		orderListModel.addElement(createRepeatingLine("----------", 30));
		orderListModel.addElement(createLine("Total", total));
		
		paymentListener.makeChange();
	}
	
	private String createRepeatingLine(String s, int length) {
		String format = "%" + length + "s";
		return String.format(format, s);
	}
	
	private String createLine(String name, int price) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format("%-20s", name));
		String amount = String.format("%#.2f", ((double) price * 0.01d));
		builder.append(String.format("%10s", ("$" + amount)));
		
		return builder.toString();
	}
	
	private String createLine(String name, double value) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format("%-20s", name));
		String amount = String.format("%#.2f", value);
		builder.append(String.format("%10s", ("$" + amount)));
		
		return builder.toString();
	}
	
	private JPanel createOrderPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		
		JPanel innerPanel = new JPanel(new GridBagLayout());
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font titleFont = panel.getFont().deriveFont(Font.BOLD, 32f);
		Font listFont = new Font("monospaced", Font.PLAIN, 16);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		JLabel label = new JLabel("Order");
		label.setFont(titleFont);
		innerPanel.add(label, gbc);
		
		gbc.gridy++;
		orderListModel.addElement(createRepeatingLine(" ", 32));
		JList<String> orderList = new JList<>(orderListModel);
		orderList.setFont(listFont);
		orderList.setVisibleRowCount(12);
		JScrollPane scrollPane = new JScrollPane(orderList);
		innerPanel.add(scrollPane, gbc);
		
		panel.add(innerPanel);
		
		return panel;
	}
	
	private JPanel createPaymentPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		
		JPanel innerPanel = new JPanel(new GridBagLayout());
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font titleFont = panel.getFont().deriveFont(Font.BOLD, 32f);
		Font listFont = new Font("monospaced", Font.PLAIN, 16);
		Font font = panel.getFont().deriveFont(16f);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 0d;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		JLabel label = new JLabel("Payment");
		label.setFont(titleFont);
		innerPanel.add(label, gbc);
		
		gbc.gridwidth = 1;
		gbc.gridy++;
		JLabel totalLabel = new JLabel("Total:");
		totalLabel.setFont(font);
		innerPanel.add(totalLabel, gbc);
		
		gbc.weightx = 1d;
		gbc.gridx++;
		totalField = new JTextField(10);
		totalField.setEditable(false);
		totalField.setFont(font);
		totalField.setHorizontalAlignment(JTextField.TRAILING);
		innerPanel.add(totalField, gbc);
		
		gbc.weightx = 0d;
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel paymentLabel = new JLabel("Payment:");
		paymentLabel.setFont(font);
		innerPanel.add(paymentLabel, gbc);
		
		gbc.weightx = 1d;
		gbc.gridx++;
		paymentField = new JTextField(10);
		paymentField.setFont(font);
		paymentField.setHorizontalAlignment(JTextField.TRAILING);
		innerPanel.add(paymentField, gbc);
		
		gbc.gridwidth = 2;
		gbc.weightx = 1d;
		gbc.gridx = 0;
		gbc.gridy++;
		JButton button = new JButton("Take Payment");
		button.addActionListener(paymentListener);
		button.setFont(font);
		innerPanel.add(button, gbc);
		
		gbc.gridwidth = 1;
		gbc.weightx = 0d;
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel changeLabel = new JLabel("Change:");
		changeLabel.setFont(font);
		innerPanel.add(changeLabel, gbc);
		
		gbc.weightx = 1d;
		gbc.gridx++;
		changeField = new JTextField(10);
		changeField.setEditable(false);
		changeField.setFont(font);
		changeField.setHorizontalAlignment(JTextField.TRAILING);
		innerPanel.add(changeField, gbc);
		
		gbc.gridwidth = 2;
		gbc.weightx = 0d;
		gbc.gridx = 0;
		gbc.gridy++;
		label = new JLabel("Change");
		label.setFont(titleFont);
		innerPanel.add(label, gbc);
		
		gbc.gridy++;
		paymentListModel.addElement(createRepeatingLine(" ", 32));
		JList<String> orderList = new JList<>(paymentListModel);
		orderList.setFont(listFont);
		orderList.setVisibleRowCount(8);
		JScrollPane scrollPane = new JScrollPane(orderList);
		innerPanel.add(scrollPane, gbc);
		
		gbc.weightx = 1d;
		gbc.gridy++;
		JButton clearButton = new JButton("Clear Order");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				order.clearItems();
				order.setOrderTotal(0d);
				orderListModel.removeAllElements();
				paymentListModel.removeAllElements();
				totalField.setText("");
				paymentField.setText("");
				changeField.setText("");
			}
		});
		clearButton.setFont(font);
		innerPanel.add(clearButton, gbc);
		
		panel.add(innerPanel);
		
		return panel;
	}
	
	public class OrderListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			JButton button = (JButton) event.getSource();
			if (button.getText().equals("Remove Item")) {
				removeItem(Integer.valueOf(event.getActionCommand()));
			} else {
				addItem(Integer.valueOf(event.getActionCommand()));
			}
			updateOrderPanel();
		}
		
		private void removeItem(int index) {
			Item item = inventory.getItem(index);
			order.removeItem(item);
		}
		
		private void addItem(int index) {
			Item item = inventory.getItem(index);
			order.addItem(item);
		}
		
	}
	
	public class PaymentListener implements ActionListener {
		
		private Object[][] currency = { { 2000, 1000, 500, 100, 25, 10, 5, 1 },
				{ "Twenty Dollar Bill", "Ten Bollar Bill", "Five Dollar Bill", 
				"One Dollar Bill", "Quarter", "Dime", "Nickel", "Penny" } };

		@Override
		public void actionPerformed(ActionEvent event) {
			makeChange();
		}
		
		public void makeChange() {
			double total = order.getOrderTotal();
			double payment = valueOf(paymentField.getText().trim());
			
			paymentField.setForeground(Color.BLACK);
			if (payment == Double.MIN_VALUE) {
				paymentField.setForeground(Color.RED);
				return;
			}
			
			if (payment < total) {
				paymentField.setForeground(Color.RED);
				return;
			}
			
			double change = payment - total;
			paymentField.setText(String.format("%#.2f", payment));
			changeField.setText(String.format("%#.2f", change));
			paymentListModel.removeAllElements();
			
			if (change > 0d) {
				int pennies = (int) Math.round(change * 100d);
				int index = 0;
				
				while (pennies > 0) {
					int denomination = (Integer) currency[0][index];
					if (pennies < denomination) {
						index++;
					} else {
						int count = pennies / denomination;
						String line = createChangeLine(count, 
								(String) currency[1][index]);
						paymentListModel.addElement(line);
						pennies -= count * denomination;
						index++;
					}
				}
			}
		}
		
		private double valueOf(String number) {
			try {
				return Double.valueOf(number);
			} catch (NumberFormatException e) {
				return Double.MIN_VALUE;
			}
		}
		
		private String createChangeLine(int amount, String denomination) {
			StringBuilder builder = new StringBuilder();
			
			builder.append(String.format("%3d", amount));
			builder.append(" ");
			
			if (amount == 1) {
				builder.append(String.format("%-20s", denomination));
			} else {
				if (denomination.equals("Penny")) {
					builder.append(String.format("%-20s", "Pennies"));
				} else {
					builder.append(String.format("%-20s", (denomination + "s")));
				}
			}
			
			return builder.toString();
		};
		
	}
	
	public class Order {
		
		private double orderTotal;
		
		private final List<Item> items;
		
		public Order() {
			this.items = new ArrayList<>();
			this.orderTotal = 0d;
		}
		
		public void clearItems() {
			this.items.clear();
		}
		
		public void addItem(Item item) {
			this.items.add(item);
		}
		
		public void removeItem(Item item) {
			this.items.remove(item);
		}
		
		public double getOrderTotal() {
			return orderTotal;
		}

		public void setOrderTotal(double orderTotal) {
			this.orderTotal = orderTotal;
		}


		public List<Item> getItems() {
			Collections.sort(items, new Comparator<Item>() {
				@Override
				public int compare(Item o1, Item o2) {
					return o1.getOrderIndex() - o2.getOrderIndex();
				}
			});
			return items;
		}
		
	}
	
	public class Inventory {
		
		private final List<Item> items;
		
		public Inventory() {
			BufferedImage image = getImage();
			this.items = createItems(image);
		}
		
		private List<Item> createItems(BufferedImage mainImage) {
			List<Item> items = new ArrayList<>();
			
			Item item = new Item("Hamburger", 3.99, 1);
			item.setImage(mainImage.getSubimage(663, 524, 320, 252)
					.getScaledInstance(160, 126, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("Hot Dog", 1.99, 2);
			item.setImage(mainImage.getSubimage(948, 933, 320, 288)
					.getScaledInstance(160, 144, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("Pizza Slice", 2.79, 3);
			item.setImage(mainImage.getSubimage(946, 111, 270, 256)
					.getScaledInstance(135, 128, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("2 Chicken Mixed", 2.49, 4);
			item.setImage(mainImage.getSubimage(352, 109, 190, 270)
					.getScaledInstance(95, 135, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("2 Chicken Dark", 1.49, 5);
			item.setImage(mainImage.getSubimage(352, 109, 190, 270)
					.getScaledInstance(95, 135, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("2 Chicken White", 3.09, 6);
			item.setImage(mainImage.getSubimage(352, 109, 190, 270)
					.getScaledInstance(95, 135, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("Salad", 2.49, 7);
			item.setImage(mainImage.getSubimage(316, 548, 342, 230)
					.getScaledInstance(158, 115, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("Fries", .99, 8);
			item.setImage(mainImage.getSubimage(32, 889, 250, 286)
					.getScaledInstance(125, 143, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("Onion Rings", 1.99, 9);
			item.setImage(mainImage.getSubimage(562, 993, 390, 166)
					.getScaledInstance(195, 83, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("Soft Ice Cream", .79, 10);
			item.setImage(mainImage.getSubimage(1006, 458, 216, 310)
					.getScaledInstance(108, 155, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("Soda", .99, 11);
			item.setImage(mainImage.getSubimage(55, 25, 196, 296)
					.getScaledInstance(98, 148, Image.SCALE_SMOOTH));
			items.add(item);
			
			item = new Item("Coffee", .69, 12);
			item.setImage(mainImage.getSubimage(319, 876, 230, 286)
					.getScaledInstance(115, 143, Image.SCALE_SMOOTH));
			items.add(item);
			
			return items;
		}
		
		public Item getItem(int index) {
			return items.get(index);
		}

		public List<Item> getItems() {
			return items;
		}
		
		private BufferedImage getImage() {
			try {
				return ImageIO.read(getClass().getResourceAsStream(
						"/fastfoodimages.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
	
	public class Item {
		
		private final int price;
		private final int orderIndex;
		
		private Image image;
		
		private final String name;

		public Item(String name, double price, int orderIndex) {
			this.price = (int) Math.round(price * 100d);
			this.name = name;
			this.orderIndex = orderIndex;
		}

		public Image getImage() {
			return image;
		}

		public void setImage(Image image) {
			this.image = image;
		}

		public int getOrderIndex() {
			return orderIndex;
		}

		public int getPrice() {
			return price;
		}

		public String getName() {
			return name;
		}
		
	}

}
