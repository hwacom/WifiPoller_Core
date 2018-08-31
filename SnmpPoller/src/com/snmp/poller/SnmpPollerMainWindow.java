package com.snmp.poller;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.snmp.poller.enums.MsgLevel;
import com.snmp.poller.init.InitialConfig;
import com.snmp.poller.service.PollerService;
import com.snmp.poller.service.impl.PollerServiceImpl;
import com.snmp.poller.utils.impl.CommonUtils;

public class SnmpPollerMainWindow {

	private static final String UDP_ADDR = "udp:(IP)/(PORT)";

	private static PollerService pollerService;

	private JFrame frmWifipoller;
	private static JTextField inputIpField;
	private static JTextField statusField;
	private static JTable dataTable;
	private static JTextPane msgPane;
	private static JScrollPane msgScrollPane;

	private final static String FONT_NAME = "\u5FAE\u8EDF\u6B63\u9ED1\u9AD4"; //微軟正黑體
	private static JTextPane pingResultPane;
	private static JTextField inputCommField;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					SnmpPollerMainWindow window = new SnmpPollerMainWindow();
					window.frmWifipoller.setVisible(true);
					init();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static void init() {
		CommonUtils.setMsgPane(msgPane);
		CommonUtils.setScrollPane(msgScrollPane);
		CommonUtils.setDataTable(dataTable);
		CommonUtils.setStatusField(statusField);
		CommonUtils.outputMsg(MsgLevel.NORMAL, SnmpPollerMainWindow.class, "初始化開始...");

		InitialConfig initConfig = new InitialConfig();
		initConfig.init();
		CommonUtils.outputMsg(MsgLevel.NORMAL, SnmpPollerMainWindow.class, "初始化完成!");

		inputIpField.setText(Env.TARGET_UDP_ADDR);
		inputCommField.setText(Env.TARGET_COMMUNITY);
	}

	private static void showPopup(int messageType, String message, String title) {
		JLabel label = new JLabel(message);
		label.setFont(new Font(FONT_NAME, Font.BOLD, 18));
		JOptionPane.showMessageDialog(null, label, title, messageType);
	}

	private static void doPing(final String ipAddr) {
		if (StringUtils.isBlank(ipAddr)) {
			showPopup(JOptionPane.WARNING_MESSAGE, "請先輸入IP !!", "Warining");

		} else {
			boolean success = CommonUtils.pingIpAddr(ipAddr);

			StyledDocument doc = pingResultPane.getStyledDocument();
			SimpleAttributeSet center = new SimpleAttributeSet();
			StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
			StyleConstants.setForeground(center, success ? Color.GREEN : Color.RED);
			doc.setParagraphAttributes(0, doc.getLength(), center, false);
			try {
				doc.remove(0, doc.getLength());
				doc.insertString(0, success ? "Success" : "Failed", center);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create the application.
	 */
	public SnmpPollerMainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmWifipoller = new JFrame();
		frmWifipoller.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				Scheduler scheduler;
				try {
					scheduler = StdSchedulerFactory.getDefaultScheduler();
					scheduler.shutdown();

				} catch (SchedulerException e) {
					e.printStackTrace();
					scheduler = null;
				}
			}
		});
		frmWifipoller.setFont(new Font("微軟正黑體", Font.PLAIN, 15));
		frmWifipoller.setTitle("Wifi Poller System");
		frmWifipoller.setBackground(Color.WHITE);
		frmWifipoller.getContentPane().setBackground(new Color(255, 255, 255));

		JPanel panel = new JPanel();
		panel.setBackground(new Color(255, 215, 0));
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));

		JLabel lblap = new JLabel("Target AP IP:");
		lblap.setBackground(Color.WHITE);
		lblap.setFont(new Font(FONT_NAME, lblap.getFont().getStyle(), 18));

		inputIpField = new JTextField();
		inputIpField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == 10) {
					inputCommField.requestFocus();
				}
			}
		});
		inputIpField.setFont(new Font("\u5FAE\u8EDF\u6B63\u9ED1\u9AD4", inputIpField.getFont().getStyle(), 18));
		inputIpField.setColumns(10);

		pingResultPane = new JTextPane();
		pingResultPane.setForeground(Color.YELLOW);
		pingResultPane.setEditable(false);
		pingResultPane.setFont(new Font("\u5FAE\u8EDF\u6B63\u9ED1\u9AD4", pingResultPane.getFont().getStyle() | Font.BOLD, 18));
		pingResultPane.setBackground(Color.BLACK);

		JButton btnPing = new JButton("Ping!");
		btnPing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				doPing(inputIpField.getText());
			}
		});
		btnPing.setBackground(UIManager.getColor("Button.light"));
		btnPing.setFont(new Font(FONT_NAME, btnPing.getFont().getStyle(), 18));

		JButton btnStart = new JButton("\u555F\u52D5");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				if (StringUtils.isBlank(inputIpField.getText().trim()) || StringUtils.isBlank(inputCommField.getText().trim())) {
					showPopup(JOptionPane.WARNING_MESSAGE, "請輸入IP及Community !!", "Warining");
					inputIpField.requestFocus();
					return;
				}

				if (pollerService == null) {
					pollerService = new PollerServiceImpl();
				}

				if (StringUtils.equals(btnStart.getText(), "啟動")) {
					final String ipAddr = inputIpField.getText();
					final String community = inputCommField.getText();

					inputCommField.setEditable(false);
					inputIpField.setEditable(false);

					Env.TARGET_UDP_ADDR = UDP_ADDR.replace("(IP)", ipAddr).replace("(PORT)", Env.TARGET_PORT);
					Env.TARGET_COMMUNITY = community;

					pollerService.startUpJobs();
					btnStart.setText("暫停");
					CommonUtils.outputMsg(MsgLevel.WARNING, SnmpPollerMainWindow.class, "Start polling..  ["+Env.TARGET_UDP_ADDR+"] ["+Env.TARGET_COMMUNITY+"]");

				} else {
					pollerService.pauseJobs();
					btnStart.setText("啟動");
					CommonUtils.outputMsg(MsgLevel.WARNING, SnmpPollerMainWindow.class, "Stop polling!");

					inputIpField.setEditable(true);
					inputCommField.setEditable(true);
				}
			}
		});
		btnStart.setFont(new Font(FONT_NAME, btnStart.getFont().getStyle(), 25));

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(143, 188, 143));
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));

		JLabel label = new JLabel("\u66F4\u65B0\u6642\u9593:");
		label.setFont(new Font(FONT_NAME, label.getFont().getStyle(), 18));
		label.setBackground(Color.WHITE);

		statusField = new JTextField();
		statusField.setForeground(Color.BLUE);
		statusField.setFont(new Font("\u5FAE\u8EDF\u6B63\u9ED1\u9AD4", statusField.getFont().getStyle(), 18));
		statusField.setBackground(UIManager.getColor("TextField.highlight"));
		statusField.setEditable(false);
		statusField.setColumns(10);

		msgScrollPane = new JScrollPane();

		msgPane = new JTextPane();
		msgPane.setForeground(Color.BLACK);
		msgPane.setBackground(Color.DARK_GRAY);
		msgPane.setFont(new Font("\u5FAE\u8EDF\u6B63\u9ED1\u9AD4", msgPane.getFont().getStyle(), 18));
		msgScrollPane.setViewportView(msgPane);

		TableModel tableModel = new DefaultTableModel(Env.TABLE_HEADER_COLUMN_NAMES, 10);
		/*
		dataTable = new JTable(tableModel){
			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				return getPreferredSize().width < getParent().getWidth();
			}
		};
		 */

		dataTable = new JTable(tableModel);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		dataTable.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
		dataTable.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);
		dataTable.getColumnModel().getColumn(10).setCellRenderer(rightRenderer);
		dataTable.getColumnModel().getColumn(11).setCellRenderer(rightRenderer);
		dataTable.getColumnModel().getColumn(14).setCellRenderer(rightRenderer);
		dataTable.getColumnModel().getColumn(15).setCellRenderer(rightRenderer);
		dataTable.getColumnModel().getColumn(16).setCellRenderer(rightRenderer);
		dataTable.getColumnModel().getColumn(17).setCellRenderer(rightRenderer);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		dataTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(12).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(13).setCellRenderer(centerRenderer);

		dataTable.setColumnSelectionAllowed(true);
		dataTable.setCellSelectionEnabled(true);
		dataTable.setFont(new Font("\u5FAE\u8EDF\u6B63\u9ED1\u9AD4", dataTable.getFont().getStyle(), 18));
		dataTable.getTableHeader().setFont(new Font("\u5FAE\u8EDF\u6B63\u9ED1\u9AD4", Font.BOLD, 18));
		dataTable.setRowHeight(25);
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		//		dataTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		JScrollPane tableScrollPane = new JScrollPane(dataTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tableScrollPane.setViewportView(dataTable);

		GroupLayout groupLayout = new GroupLayout(frmWifipoller.getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(14)
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
												.addGroup(groupLayout.createSequentialGroup()
														.addComponent(panel, GroupLayout.PREFERRED_SIZE, 808, Short.MAX_VALUE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 291, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(btnStart, GroupLayout.PREFERRED_SIZE, 159, GroupLayout.PREFERRED_SIZE))
												.addComponent(msgScrollPane, GroupLayout.DEFAULT_SIZE, 1274, Short.MAX_VALUE)))
								.addGroup(groupLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(tableScrollPane, GroupLayout.DEFAULT_SIZE, 1274, Short.MAX_VALUE)))
						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(panel, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(12)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(btnStart, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
												.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE))))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(tableScrollPane, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(msgScrollPane, GroupLayout.PREFERRED_SIZE, 245, GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
				);
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
						.addGap(7)
						.addComponent(label, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
						.addGap(5)
						.addComponent(statusField, GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
						.addGap(9))
				);
		gl_panel_1.setVerticalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_1.createSequentialGroup()
										.addGap(12)
										.addComponent(label))
								.addGroup(gl_panel_1.createSequentialGroup()
										.addGap(9)
										.addComponent(statusField, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		panel_1.setLayout(gl_panel_1);

		JLabel lblCommunity = new JLabel("Community:");
		lblCommunity.setFont(new Font("\u5FAE\u8EDF\u6B63\u9ED1\u9AD4", lblCommunity.getFont().getStyle(), 18));
		lblCommunity.setBackground(Color.WHITE);

		inputCommField = new JTextField();
		inputCommField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				doPing(inputIpField.getText());
			}
		});
		inputCommField.setFont(new Font("\u5FAE\u8EDF\u6B63\u9ED1\u9AD4", inputCommField.getFont().getStyle(), 18));
		inputCommField.setColumns(10);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
						.addGap(7)
						.addComponent(lblap, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(inputIpField, GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(lblCommunity, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(inputCommField, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnPing, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
						.addGap(7)
						.addComponent(pingResultPane, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
				);
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
						.addGap(7)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addComponent(btnPing)
								.addComponent(inputCommField, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
								.addComponent(lblCommunity, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
								.addComponent(inputIpField, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
								.addComponent(lblap, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
								.addComponent(pingResultPane, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
						.addContainerGap())
				);
		panel.setLayout(gl_panel);
		frmWifipoller.getContentPane().setLayout(groupLayout);
		frmWifipoller.setBounds(100, 100, 1320, 750);
		frmWifipoller.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setForeground(Color.WHITE);
		menuBar.setBackground(SystemColor.inactiveCaption);
		frmWifipoller.setJMenuBar(menuBar);

		JMenu menu = new JMenu("\u6A94\u6848");
		menu.setFont(new Font(FONT_NAME, menu.getFont().getStyle(), 18));
		menuBar.add(menu);

		JMenuItem menuItem_Load = new JMenuItem("\u8B80\u53D6\u8A2D\u5B9A\u6A94");
		menuItem_Load.setFont(new Font(FONT_NAME, menuItem_Load.getFont().getStyle(), 18));
		menu.add(menuItem_Load);

		JMenuItem menuItem_Exit = new JMenuItem("\u96E2\u958B");
		menuItem_Exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				frmWifipoller.dispatchEvent(new WindowEvent(frmWifipoller, WindowEvent.WINDOW_CLOSING));
			}
		});
		menuItem_Exit.setFont(new Font(FONT_NAME, menuItem_Exit.getFont().getStyle(), 18));
		menu.add(menuItem_Exit);
	}

	private void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 15; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width +1 , width);
			}
			if(width > 300) {
				width=300;
			}
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}
}
