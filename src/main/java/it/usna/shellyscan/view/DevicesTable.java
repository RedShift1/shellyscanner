package it.usna.shellyscan.view;

import static it.usna.shellyscan.Main.LABELS;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.usna.shellyscan.Main;
import it.usna.shellyscan.model.device.InternalTmpHolder;
import it.usna.shellyscan.model.device.LabelHolder;
import it.usna.shellyscan.model.device.Meters;
import it.usna.shellyscan.model.device.ShellyAbstractDevice;
import it.usna.shellyscan.model.device.ShellyAbstractDevice.Status;
import it.usna.shellyscan.model.device.g1.ShellyDW;
import it.usna.shellyscan.model.device.g1.ShellyFlood;
import it.usna.shellyscan.model.device.g1.ShellyMotion;
import it.usna.shellyscan.model.device.g1.ShellyTRV;
import it.usna.shellyscan.model.device.g1.modules.LightBulbRGBCommander;
import it.usna.shellyscan.model.device.g1.modules.Thermostat;
import it.usna.shellyscan.model.device.g2.ShellyPlusSmoke;
import it.usna.shellyscan.model.device.modules.DeviceModule;
import it.usna.shellyscan.model.device.modules.InputCommander;
import it.usna.shellyscan.model.device.modules.RGBWCommander;
import it.usna.shellyscan.model.device.modules.RelayCommander;
import it.usna.shellyscan.model.device.modules.RollerCommander;
import it.usna.shellyscan.model.device.modules.WhiteCommander;
import it.usna.swing.ArrayTableCellRenderer;
import it.usna.swing.DecimalTableCellRenderer;
import it.usna.swing.table.ExTooltipTable;
import it.usna.swing.table.UsnaTableModel;

public class DevicesTable extends ExTooltipTable {
	private static final long serialVersionUID = 1L;
	private final static URL OFFLINEIMG = MainView.class.getResource("/images/bullet_stop.png");
	public final static ImageIcon ONLINE_BULLET = new ImageIcon(MainView.class.getResource("/images/bullet_yes.png"), LABELS.getString("labelDevOnLIne"));
	public final static ImageIcon ONLINE_BULLET_REBOOT = new ImageIcon(MainView.class.getResource("/images/bullet_yes_reboot.png"), LABELS.getString("labelDevOnLIneReboot"));
	public final static ImageIcon OFFLINE_BULLET = new ImageIcon(OFFLINEIMG, LABELS.getString("labelDevOffLIne"));
	public final static ImageIcon LOGIN_BULLET = new ImageIcon(MainView.class.getResource("/images/bullet_star_yellow.png"), LABELS.getString("labelDevNotLogged"));
	public final static ImageIcon UPDATING_BULLET = new ImageIcon(MainView.class.getResource("/images/bullet_refresh.png"), LABELS.getString("labelDevUpdating"));
	public final static ImageIcon ERROR_BULLET = new ImageIcon(MainView.class.getResource("/images/bullet_error.png"), LABELS.getString("labelDevError"));
	public final static ImageIcon GHOST_BULLET = new ImageIcon(MainView.class.getResource("/images/bullet_ghost.png"), LABELS.getString("labelDevGhost"));
	private final static String TRUE = LABELS.getString("true_yn");
	private final static String FALSE = LABELS.getString("false_yn");
	private final static String YES = LABELS.getString("true_yna");
	private final static String NO = LABELS.getString("false_yna");
	private final static MessageFormat SWITCH_FORMATTER = new MessageFormat(Main.LABELS.getString("METER_VAL_EXS"), Locale.ENGLISH); // tooltip
	
	// model columns indexes
	final static int COL_STATUS_IDX = 0;
	final static int COL_TYPE = 1;
	final static int COL_NAME = 2;
	final static int COL_MAC_IDX = 3;
	final static int COL_IP_IDX = 4;
	final static int COL_SSID_IDX = 5;
	final static int COL_RSSI_IDX = 6;
	final static int COL_CLOUD = 7;
	final static int COL_MQTT = 8;
	final static int COL_UPTIME_IDX = 9;
	final static int COL_INT_TEMP = 10;
	final static int COL_MEASURES_IDX = 11;
	final static int COL_DEBUG = 12;
	final static int COL_SOURCE_IDX = 13;
	final static int COL_COMMAND_IDX = 14;

	private boolean adaptTooltipLocation = false;
	
	private final static Logger LOG = LoggerFactory.getLogger(DevicesTable.class);

	public DevicesTable(TableModel tm) {
		super(tm, true);
		columnModel.getColumn(COL_STATUS_IDX).setMaxWidth(ONLINE_BULLET.getIconWidth() + 4);
		columnModel.getColumn(COL_MEASURES_IDX).setCellRenderer(new DeviceMetersCellRenderer());
		columnModel.getColumn(COL_INT_TEMP).setCellRenderer(new DecimalTableCellRenderer(2));
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		columnModel.getColumn(COL_CLOUD).setCellRenderer(centerRenderer);
		columnModel.getColumn(COL_MQTT).setCellRenderer(centerRenderer);
		columnModel.getColumn(COL_DEBUG).setCellRenderer(centerRenderer);
		columnModel.getColumn(COL_SOURCE_IDX).setCellRenderer(new ArrayTableCellRenderer());
		final TableColumn colCommand = columnModel.getColumn(COL_COMMAND_IDX);
		colCommand.setCellRenderer(new DevicesCommandCellRenderer());
		colCommand.setCellEditor(new DeviceTableCellEditor(this));

		TableRowSorter<?> sorter = (TableRowSorter<?>)getRowSorter();
		
		sorter.setComparator(COL_COMMAND_IDX, (o1, o2) -> {
			final String s1, s2;
			if(o1 == null) {
				s1 = "";
			} else if (o1 instanceof DeviceModule[]) {
				s1 = ((DeviceModule[])o1)[0].getLabel();
			} else if (o1 instanceof DeviceModule) {
				s1 = ((DeviceModule)o1).getLabel();
			} else {
				s1 = o1.toString();
			}
			if(o2 == null) {
				s2 = "";
			} else if (o2 instanceof DeviceModule[]) {
				s2 = ((DeviceModule[])o2)[0].getLabel();
			} else if (o1 instanceof DeviceModule) {
				s2 = ((DeviceModule)o2).getLabel();
			} else {
				s2 = o2.toString();
			}
			return s1.compareTo(s2);
		});
		
		sorter.setComparator(COL_MEASURES_IDX, (Meters[] o1, Meters[] o2) -> {
			if(o1 == null) {
				return -1;
			}
			if(o2 == null) {
				return 1;
			}
//			return ((Comparable<Object>[])o1)[0].compareTo(((Comparable<Object>[])o2)[0]);
			return (o1[0]).compareTo(o2[0]);
		});
		
		sorter.setComparator(COL_SOURCE_IDX, (o1, o2) -> {
			String s1 = o1 instanceof String[] ? ((String[])o1)[0] : (String)o1;
			String s2 = o2 instanceof String[] ? ((String[])o2)[0] : (String)o2;
			if(s1 == null) {
				return -1;
			}
			if(s2 == null) {
				return 1;
			}
			return s1.compareTo(s2);
		});
		
		getActionMap().put("copy", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				Object cellValue = getValueAt(getSelectedRow(), getSelectedColumn());
				StringSelection stringSelection = new StringSelection(cellTooltipValue(cellValue, true, 0, 0));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
			}
		});
		
		String[] headerTips = new String[COL_MQTT + 1];
		headerTips[COL_STATUS_IDX] = LABELS.getString("col_status_exp");
		headerTips[COL_CLOUD] = LABELS.getString("col_cloud_exp");
		headerTips[COL_MQTT] = LABELS.getString("col_mqtt_exp");
		setHeadersTooltip(headerTips);

		// sorter.setSortsOnUpdates(true); // messes rows heights and selection
        // setUpdateSelectionOnSort(true); // default
	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		Object val = getValueAt(row, column);
		return val instanceof DeviceModule || val instanceof DeviceModule[];
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component comp = super.prepareRenderer(renderer, row, column);
		if(isRowSelected(row) == false) {
			if(row % 2 == 0 ) {
				comp.setBackground(Main.TAB_LINE1);
			} else {
				comp.setBackground(Main.TAB_LINE2);
			}
		} else {
			comp.setBackground(getSelectionBackground());
//			comp.setForeground(getSelectionForeground());
		}
		computeRowHeight(row, comp);
		return comp;
	}

	public void stopCellEditing() {
		TableCellEditor editor = getCellEditor();
		if(editor != null) {
			editor.stopCellEditing();
		}
	}

	@Override
	public String getToolTipText(final MouseEvent evt) {
		final int r, c;
		final Object value;
		if(((Component) evt.getSource()).isVisible() && (r = rowAtPoint(evt.getPoint())) >= 0 && (c = columnAtPoint(evt.getPoint())) >= 0 && (value = getValueAt(r, c)) != null) {
			final int modelCol = convertColumnIndexToModel(c);
			final String ret;
			if(modelCol == COL_UPTIME_IDX) {
				adaptTooltipLocation = false;
				long s = ((Number)value).longValue();
				final int gg = (int)(s / (3600 * 24));
				s = s % (3600 * 24);
				int hh = (int)(s / 3600);
				s = s % 3600;
				int mm = (int)(s / 60);
				s = s % 60;
				return String.format(LABELS.getString("col_uptime_tooltip"), gg, hh, mm, s);
			} else if (value instanceof ImageIcon) {
				adaptTooltipLocation = false;
				return ((ImageIcon)value).getDescription();
			} else if(value instanceof DeviceModule[] && isColumnVisible(COL_SOURCE_IDX) == false && ((DeviceModule[])value).length > 0 && ((DeviceModule[])value)[0].getLastSource() != null) {
				adaptTooltipLocation = false;
				return Arrays.stream((DeviceModule[])value).
						map(rel -> String.format(LABELS.getString("col_last_source_tooltip"), rel, rel.getLastSource())).collect(Collectors.joining("<br>", "<html>", "</html>"));
			} else if(value instanceof DeviceModule && isColumnVisible(COL_SOURCE_IDX) == false && (ret = ((DeviceModule)value).getLastSource()) != null) {
				adaptTooltipLocation = false;
				return "<html>" + String.format(LABELS.getString("col_last_source_tooltip"), value, ret) + "</html>";
			} else if(value instanceof Thermostat) {
				adaptTooltipLocation = false;
				return String.format(Locale.ENGLISH, LABELS.getString("col_command_therm_tooltip"), ((Thermostat)value).getCurrentProfile(), ((Thermostat)value).getTargetTemp(), ((Thermostat)value).getPosition());
			} else if(value instanceof Meters[]) {
				Component comp = getCellRenderer(r, c).getTableCellRendererComponent(this, value, false, false, r, c);
				if(Arrays.stream((Meters[])value).anyMatch(m -> m instanceof LabelHolder) || getCellRect(r, c, false).width <= comp.getPreferredSize().width) {
					adaptTooltipLocation = true;
					String tt = "<html><table border='0' cellspacing='0' cellpadding='0'>";
					for(Meters m: (Meters[])value) {
						tt += "<tr>";
						if(m instanceof LabelHolder) {
							tt += "<td><b>" + ((LabelHolder)m).getLabel() + "</b>&nbsp;</td>";
						}
						for(Meters.Type t: m.getTypes()) {
							if(t == Meters.Type.EXS) {
								tt += "<td><i>" +  LABELS.getString("METER_LBL_" + t) + "</i>&nbsp;</td><td align='right'>" + SWITCH_FORMATTER.format(new Object [] {m.getValue(t)}) + "&nbsp;</td>";
							} else {
								tt += "<td><i>" +  LABELS.getString("METER_LBL_" + t) + "</i>&nbsp;</td><td align='right'>" + String.format(Locale.ENGLISH, LABELS.getString("METER_VAL_" + t), m.getValue(t)) + "&nbsp;</td>";
							}
						}
						tt += "</tr>";
					}
					return tt + "</table></html>";
				}
			} else {
				adaptTooltipLocation = true;
				return super.getToolTipText(evt);
			}
		}
		return null;
	}

	@Override
	public Point getToolTipLocation(final MouseEvent evt) {
		return (adaptTooltipLocation) ? super.getToolTipLocation(evt) : null;
	}

	@Override
	public void columnsWidthAdapt() {
		Graphics g = getGraphics();
		if(g != null) {
			final FontMetrics fm = g.getFontMetrics();
			for(int c = 0; c < getColumnCount(); c++) {
				TableColumn tc = columnModel.getColumn(c);
				Object val = tc.getHeaderValue();
				int width = val != null ? SwingUtilities.computeStringWidth(fm, val.toString()) / 2 : 1; // "/2"
				for(int r = 0; r < getRowCount(); r++) {
					val = getValueAt(r, c);
					if(val != null) {
						if(val instanceof Icon) {
							width = Math.max(width, ((Icon)val).getIconWidth());
						} else if(val instanceof Object[]) {
							for(Object v: (Object[])val) {
								int w = SwingUtilities.computeStringWidth(fm, v.toString());
								width = Math.max(width, w);
							}
						} else {
							width = Math.max(width, SwingUtilities.computeStringWidth(fm, val.toString()));
						}
					}
				}
				tc.setPreferredWidth(width);
			}
		}
	}

	public void csvExport(BufferedWriter w, String separator) throws IOException {
		Stream.Builder<String> h = Stream.builder();
		for(int col = 0; col < getColumnCount(); col++) {
			String name = getColumnName(col);
			h.accept(name.length() == 0 ? LABELS.getString("col_status_exp") : name); // dirty and fast
		}
		w.write(h.build().collect(Collectors.joining(separator)));
		w.newLine();

		for(int row = 0; row < getRowCount(); row++) {
			Stream.Builder<String> r = Stream.builder();
			for(int col = 0; col < getColumnCount(); col++) {
				r.accept(cellTooltipValue(getValueAt(row, col), true, row, col));
			}
			w.write(r.build().collect(Collectors.joining(separator)));
			w.newLine();
		}
	}

	public void setRowFilter(String filter, int ... cols) {
		TableRowSorter<?> sorter = (TableRowSorter<?>)getRowSorter();
		if(filter.length() > 0) {
			filter = filter.replace("\\E", "\\e");
			sorter.setRowFilter(RowFilter.regexFilter("(?i).*\\Q" + filter + "\\E.*", cols));
		} else {
			sorter.setRowFilter(null);
		}
	}

	public void resetRowsComputedHeight() {
//		Collections.fill(rowH, 1);
		for(int i = 0; i < getRowCount(); i++) {
			setRowHeight(i, 1);
		}
	}

	// adapt row height
//	private ArrayList<Integer> rowH = new ArrayList<>();
	private void computeRowHeight(int rowIndex, Component callVal) {
//		int modelRowIndex = convertRowIndexToModel(rowIndex);
//		int currentH;
//		if(modelRowIndex >= rowH.size()) {
//			while(rowH.size() <= modelRowIndex) {
//				rowH.add(1);
//			}
//			currentH = 1;
//		} else {
//			currentH = rowH.get(modelRowIndex);
//		}
//		int thisH = callVal.getPreferredSize().height;
//		if(currentH < thisH) {
//			setRowHeight(rowIndex, thisH);
//			rowH.set(modelRowIndex, thisH);
//		}
		int thisH = callVal.getPreferredSize().height;
		if(getRowHeight(rowIndex) < thisH) {
			setRowHeight(rowIndex, thisH);
		}
	}
	
	public void addRow(ShellyAbstractDevice d) {
//		resetRowsComputedHeight();
		((UsnaTableModel)dataModel).addRow(generateRow(d, new Object[DevicesTable.COL_COMMAND_IDX + 1]));
		columnsWidthAdapt();
		getRowSorter().allRowsChanged();
	}
	
	public void updateRow(ShellyAbstractDevice d, int index) {
		Object[] row = generateRow(d, ((UsnaTableModel)dataModel).getRow(index));
		((UsnaTableModel)dataModel).setRow(index, row);
		final ListSelectionModel lsm = getSelectionModel(); // allRowsChanged() do not preserve the selected cell; this mess the selection dragging the mouse
		final int i1 = lsm.getAnchorSelectionIndex();
//		final int i2 = lsm.getLeadSelectionIndex();
		getRowSorter().allRowsChanged();
		lsm.setAnchorSelectionIndex(i1);
//		lsm.setLeadSelectionIndex(i2);
	}
	
	private static Object[] generateRow(ShellyAbstractDevice d, final Object row[]) {
		try {
			row[DevicesTable.COL_STATUS_IDX] = getStatusIcon(d);
			row[DevicesTable.COL_TYPE] = d.getTypeName() + " (" + d.getHostname() + ")";
			row[DevicesTable.COL_NAME] = d.getName();
			row[DevicesTable.COL_MAC_IDX] = d.getMacAddress();
			row[DevicesTable.COL_IP_IDX] = /*d.getAddress()*/new InetAddressAndPort(d);
			row[DevicesTable.COL_SSID_IDX] = d.getSSID();
			Status status = d.getStatus();
			if(status != Status.NOT_LOOGGED && status != Status.ERROR && status != Status.GHOST /*&&(d instanceof ShellyUnmanagedDevice == false || ((ShellyUnmanagedDevice)d).geException() == null)*/) {
				row[DevicesTable.COL_RSSI_IDX] = d.getRssi();
				row[DevicesTable.COL_CLOUD] = (d.getCloudEnabled() ? TRUE : FALSE) + " " + (d.getCloudConnected() ? TRUE : FALSE);
				row[DevicesTable.COL_MQTT] = (d.getMQTTEnabled() ? TRUE : FALSE) + " " + (d.getMQTTConnected() ? TRUE : FALSE);
				row[DevicesTable.COL_UPTIME_IDX] = d.getUptime();
				row[DevicesTable.COL_INT_TEMP] = (d instanceof InternalTmpHolder) ? ((InternalTmpHolder)d).getInternalTmp() : null;
				row[DevicesTable.COL_MEASURES_IDX] = d.getMeters();
				row[DevicesTable.COL_DEBUG] = LABELS.getString("debug" + d.getDebugMode());
				Object command = null;
				if(d instanceof RelayCommander && ((RelayCommander)d).getRelayCount() > 0) {
					row[DevicesTable.COL_COMMAND_IDX] = command = ((RelayCommander)d).getRelays();
				} else if(d instanceof RollerCommander && ((RollerCommander)d).getRollerCount() > 0) {
					row[DevicesTable.COL_COMMAND_IDX] = command = ((RollerCommander)d).getRoller(0);
				} else if(d instanceof WhiteCommander && ((WhiteCommander)d).getWhiteCount() == 1) { // dimmer
					row[DevicesTable.COL_COMMAND_IDX] = command = ((WhiteCommander)d).getWhite(0);
				} else if(d instanceof LightBulbRGBCommander) {
					row[DevicesTable.COL_COMMAND_IDX] = command = ((LightBulbRGBCommander)d).getLight(0);
//					row[DevicesTable.COL_COMMAND_IDX] = new Thermostat((AbstractG1Device)d); // test
//					row[DevicesTable.COL_COMMAND_IDX] = new LightWhite[] {new LightWhite((AbstractG1Device)d, "", 0), new LightWhite((AbstractG1Device)d, "", 1), new LightWhite((AbstractG1Device)d, "", 2)}; // test
//					row[DevicesTable.COL_COMMAND_IDX] = new LightRGBW((AbstractG1Device)d, 0); // test
				} else if(d instanceof RGBWCommander && ((RGBWCommander)d).getColorCount() > 0) {
					row[DevicesTable.COL_COMMAND_IDX] = command = ((RGBWCommander)d).getColor(0);
				} else if(d instanceof WhiteCommander && ((WhiteCommander)d).getWhiteCount() > 1) {
					row[DevicesTable.COL_COMMAND_IDX] = command = ((WhiteCommander)d).getWhites();
				} else if(d instanceof InputCommander) {
					row[DevicesTable.COL_COMMAND_IDX] = ((InputCommander)d).getActionsGroups();
				} else if(d instanceof ShellyDW) {
					row[DevicesTable.COL_COMMAND_IDX] = LABELS.getString("lableStatusOpen") + ": " + (((ShellyDW)d).isOpen() ? YES : NO);
				} else if(d instanceof ShellyFlood) {
					row[DevicesTable.COL_COMMAND_IDX] = LABELS.getString("lableStatusFlood") + ": " + (((ShellyFlood)d).flood() ? YES : NO);
				} else if(d instanceof ShellyMotion) {
					row[DevicesTable.COL_COMMAND_IDX] = String.format(LABELS.getString("lableStatusMotion"), ((ShellyMotion)d).motion() ? YES : NO);
				} else if(d instanceof ShellyPlusSmoke) {
					row[DevicesTable.COL_COMMAND_IDX] = String.format(LABELS.getString("lableStatusSmoke"), ((ShellyPlusSmoke)d).getAlarm() ? YES : NO);
				} else if(d instanceof ShellyTRV) {
					Thermostat thermostat = ((ShellyTRV)d).getThermostat();
					if(thermostat.isAutoTemp()) {
						row[DevicesTable.COL_COMMAND_IDX] = thermostat;
					} else {
						row[DevicesTable.COL_COMMAND_IDX] = String.format(LABELS.getString("lableStatusTRV"), thermostat.getPosition());
					}
				}
				if(command instanceof DeviceModule) {
					row[DevicesTable.COL_SOURCE_IDX] = ((DeviceModule)command).getLastSource();
				} else if(command instanceof DeviceModule[]) {
					DeviceModule[] m = ((DeviceModule[])command);
					String res[] = new String[m.length];
					for(int i = 0; i < m.length; i++) {
						res[i] = m[i].getLastSource();
					}
					row[DevicesTable.COL_SOURCE_IDX] = res;
				}
			} else {
				row[DevicesTable.COL_RSSI_IDX] = row[DevicesTable.COL_CLOUD] = row[DevicesTable.COL_UPTIME_IDX] = row[DevicesTable.COL_INT_TEMP] =
						row[DevicesTable.COL_MEASURES_IDX] = row[DevicesTable.COL_DEBUG] = row[DevicesTable.COL_COMMAND_IDX] = null;
			}
		} catch(Exception e) {
			LOG.error("", e);
		}
		return row;
	}
	
	public static ImageIcon getStatusIcon(ShellyAbstractDevice d) {
		if(d.getStatus() == Status.ON_LINE) {
			if(/*d instanceof AbstractG2Device && ((AbstractG2Device)d).rebootRequired()*/ d.rebootRequired()) {
				return ONLINE_BULLET_REBOOT;
			} else {
				return ONLINE_BULLET;
			}
		} else if(d.getStatus() == Status.OFF_LINE) {
			long lastOnline = d.getLastTime();
			if(lastOnline > 0) {
				return new ImageIcon(OFFLINEIMG, String.format(LABELS.getString("labelDevOffLIneTime"), LocalDateTime.ofInstant(Instant.ofEpochMilli(lastOnline), TimeZone.getDefault().toZoneId())));
			} else {
				return OFFLINE_BULLET;
			}
		} else if(d.getStatus() == Status.READING) {
			return UPDATING_BULLET;
		} else if(d.getStatus() == Status.GHOST) {
			return GHOST_BULLET;
		} else if(d.getStatus() == Status.ERROR) {
			return ERROR_BULLET;
		} else { // Status.NOT_LOOGGED
			return LOGIN_BULLET;
		}
	}
} // 462 - 472 - 493