package com.snmp.poller.utils.impl;

import java.awt.Color;
import java.util.Date;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snmp.poller.Env;
import com.snmp.poller.enums.MsgLevel;
import com.snmp.poller.model.UserCurrentInfo;

public class CommonUtils {
	private static Logger log = LoggerFactory.getLogger(CommonUtils.class);

	private static JTextPane msgPane;
	private static JTextField statusField;
	private static JScrollPane scrollPane;
	private static JTable dataTable;

	public static void setMsgPane(JTextPane msgPane) {
		CommonUtils.msgPane = msgPane;
	}

	public static void setScrollPane(JScrollPane scrollPane) {
		CommonUtils.scrollPane = scrollPane;
	}

	public static void setDataTable(JTable dataTable) {
		CommonUtils.dataTable = dataTable;
	}

	public static void setStatusField(JTextField statusField) {
		CommonUtils.statusField = statusField;
	}

	public static boolean pingIpAddr(String ipAddr) {
		boolean ret = false;

		try {
			final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest ();
			request.setHost(ipAddr);

			final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);
			final String formattedResponse = IcmpPingUtil.formatResponse(response);
			CommonUtils.outputMsg(MsgLevel.WARNING, CommonUtils.class, "Ping response: "+formattedResponse);

			ret = response.getSuccessFlag();

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, CommonUtils.class, e.toString());
		}
		return ret;
	}

	public static void refreshDataTable(List<UserCurrentInfo> entities) {
		try {
			DefaultTableModel model = (DefaultTableModel)dataTable.getModel();
			model.setRowCount(0); //先清空所有資料

			int rowNo = 1;
			for (UserCurrentInfo entity : entities) {
				model.addRow(transEntityField2Array(String.valueOf(rowNo), entity));
				rowNo++;
			}

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, CommonUtils.class, e.toString());
		}
	}

	public static void refreshTime() {
		statusField.setText(Env.FORMAT_YYYYMMDDHHMISS.format(new Date()));
	}

	private static String[] transEntityField2Array(String rowNo, UserCurrentInfo entity) {
		String[] rowData = entity.toArray();
		rowData[0] = rowNo;
		return rowData;
	}

	public synchronized static void outputMsg(MsgLevel level, Class classObj, String msg) {
		try {
			StyledDocument doc = msgPane.getStyledDocument();

			final String timestamp = "[".concat(Env.FORMAT_YYYYMMDDHHMISS.format(new Date())).concat("] ");
			SimpleAttributeSet attrTimestamp = new SimpleAttributeSet();
			StyleConstants.setForeground(attrTimestamp, Color.GRAY);

			msg = msg == null ? "" : msg;
			final String message = level == MsgLevel.ERROR
					? "<".concat(classObj.getSimpleName()).concat(">").concat(": ").concat(msg.concat("\n"))
							: msg.concat("\n");
					SimpleAttributeSet attrMessage = new SimpleAttributeSet();

					Color foreground = null;
					boolean bold = true;

					switch (level) {
					case INFO:
						foreground = new Color(60, 179, 113);
						break;

					case WARNING:
						foreground = Color.ORANGE;
						break;

					case ERROR:
						foreground = Color.RED;
						break;

					default:
						foreground = Color.WHITE;
						bold = false;
						break;
					}

					StyleConstants.setForeground(attrMessage, foreground);
					StyleConstants.setBold(attrMessage, bold);

					String[] lines = doc.getText(0, doc.getLength()).split("\n");
					if (lines.length >= Env.MAX_MSG_LINES) {
						StringBuffer sb = new StringBuffer();

						int max = lines.length-1;
						for (int i=0; i< Env.MAX_MSG_LINES; i++) {
							sb.append(lines[max-i]).append("\n");
						}

						doc.remove(0, sb.length());
					}

					doc.insertString(0, message != null ? message : "", attrMessage);
					doc.insertString(0, timestamp, attrTimestamp);

					//					final int extent = scrollPane.getVerticalScrollBar().getModel().getExtent();
					//					scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()+extent);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

}
