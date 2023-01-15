package it.usna.shellyscan.view.chart;

import static it.usna.shellyscan.Main.LABELS;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.usna.shellyscan.Main;
import it.usna.shellyscan.model.Devices;
import it.usna.shellyscan.model.Devices.EventType;
import it.usna.shellyscan.model.device.InternalTmpHolder;
import it.usna.shellyscan.model.device.Meters;
import it.usna.shellyscan.model.device.ShellyAbstractDevice;
import it.usna.shellyscan.model.device.ShellyAbstractDevice.Status;
import it.usna.shellyscan.view.MainView;
import it.usna.shellyscan.view.appsettings.DialogAppSettings;
import it.usna.shellyscan.view.util.UtilCollecion;
import it.usna.util.AppProperties;
import it.usna.util.UsnaEventListener;
import javax.swing.JToggleButton;  

// https://www.javatpoint.com/jfreechart-tutorial
public class MeasuresChart extends JFrame implements UsnaEventListener<Devices.EventType, Integer> {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(MeasuresChart.class);
	private final Devices model;
	private final Map<Integer, TimeSeries[]> seriesMap = new HashMap<>();

	public enum ChartType {
		INT_TEMP("dlgChartsIntTempLabel", "dlgChartsIntTempYLabel"),
		RSSI("dlgChartsRSSILabel", "dlgChartsRSSIYLabel"),
		P("dlgChartsAPowerLabel", "dlgChartsAPowerYLabel", Meters.Type.W),
		Q("dlgChartsQPowerLabel", "dlgChartsQPowerYLabel", Meters.Type.VAR),
		V("dlgChartsVoltageLabel", "dlgChartsVoltageYLabel", Meters.Type.V),
		I("dlgChartsCurrentLabel", "dlgChartsCurrentYLabel", Meters.Type.I),
		T("dlgChartsTempLabel", "dlgChartsTempYLabel", Meters.Type.T),
		H("dlgChartsHumidityLabel", "dlgChartsHumidityYLabel", Meters.Type.H),
		LUX("dlgChartsLuxLabel", "dlgChartsLuxYLabel", Meters.Type.L);

		private final String yLabel;
		private final String label;
		private Meters.Type mType;

		private ChartType(String labelID, String yLabelID) {
			this.yLabel = LABELS.getString(yLabelID);
			this.label = LABELS.getString(labelID);
		}

		private ChartType(String labelID, String yLabelID, Meters.Type mType) {
			this.yLabel = LABELS.getString(yLabelID);
			this.label = LABELS.getString(labelID);
			this.mType = mType;
		}

		@Override
		public String toString() { // combo box
			return label;
		}
	}

	private ChartType currentType;

	public MeasuresChart(JFrame owner, final Devices model, int[] ind, AppProperties appProp) {  
		setTitle(LABELS.getString("dlgChartsTitle") + " - " + (ind.length == 1 ? UtilCollecion.getDescName(model.get(ind[0])) : ind.length));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource(Main.ICON)));
		this.model = model;

		JPanel mainPanel = new JPanel(new BorderLayout());
		this.setContentPane(mainPanel);

		// Create dataset  
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		// Create chart  
		JFreeChart chart = ChartFactory.createTimeSeriesChart(  
				null, // Chart  
				LABELS.getString("dlgChartsXLabel"), // X-Axis Label  
				"val", // Y-Axis Label  
				dataset, true, true, false);  

		XYPlot plot = chart.getXYPlot();
		// plot.setBackgroundPaint(new Color(255,255,196));

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseZoomable(false);

		mainPanel.add(chartPanel, BorderLayout.CENTER);

		JPanel commandPanel = new JPanel(new BorderLayout());
		JPanel westCommandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		commandPanel.add(westCommandPanel, BorderLayout.WEST);
		JPanel eastCommandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		commandPanel.add(eastCommandPanel, BorderLayout.EAST);
		mainPanel.add(commandPanel, BorderLayout.SOUTH);

		JButton btnClear = new JButton(LABELS.getString("dlgChartsBtnClear"));
		btnClear.addActionListener(e -> initDataSet(plot.getRangeAxis(), dataset, model, ind));
		JButton btnClose = new JButton(LABELS.getString("dlgClose"));
		btnClose.addActionListener(e -> dispose());
		eastCommandPanel.add(btnClear);
		eastCommandPanel.add(btnClose);

		JComboBox<String> rangeCombo = new JComboBox<>();
		ValueAxis xAxis = plot.getDomainAxis();
		rangeCombo.addItem(LABELS.getString("dlgChartsRangeAuto"));
		rangeCombo.addItem(LABELS.getString("dlgChartsRange1min"));
		rangeCombo.addItem(LABELS.getString("dlgChartsRange5min"));
		rangeCombo.addItem(LABELS.getString("dlgChartsRange15min"));
		rangeCombo.addItem(LABELS.getString("dlgChartsRange30min"));
		rangeCombo.addItem(LABELS.getString("dlgChartsRange60min"));

		westCommandPanel.add(new JLabel(LABELS.getString("dlgChartsRangeComboLabel")));
		westCommandPanel.add(rangeCombo);
		westCommandPanel.add(new JLabel(LABELS.getString("dlgChartsTypeComboLabel")));

		JComboBox<ChartType> typeCombo = new JComboBox<>();
		for(ChartType t: ChartType.values()) {
			typeCombo.addItem(t);
		}
		typeCombo.addActionListener(e -> {
			currentType = (ChartType)typeCombo.getSelectedItem();
			initDataSet(plot.getRangeAxis(), dataset, model, ind);
		});

		westCommandPanel.add(typeCombo);
		
		JButton btnDownload = new JButton(new ImageIcon(MeasuresChart.class.getResource("/images/Download24.png")));
		btnDownload.setContentAreaFilled(false);
		btnDownload.setToolTipText(LABELS.getString("dlgChartsCSV"));
		btnDownload.setBorder(BorderFactory.createEmptyBorder());
		btnDownload.addActionListener(e -> ChartsUtil.exportCSV(this, appProp, dataset));
		
		JToggleButton btnPause = new JToggleButton(new ImageIcon(MeasuresChart.class.getResource("/images/PlayerPause24.png")));
		btnPause.setSelectedIcon(new ImageIcon(MeasuresChart.class.getResource("/images/PlayerPlay24.png")));
		btnPause.setRolloverEnabled(false);
		btnPause.setContentAreaFilled(false);
		btnPause.setBorder(BorderFactory.createEmptyBorder());
		btnPause.addActionListener(e ->  {
			if(btnPause.isSelected()) {
				xAxis.setRange(xAxis.getRange());
			} else {
				setRange(xAxis, rangeCombo.getSelectedIndex());
			}
		});
		
		westCommandPanel.add(btnPause);
		westCommandPanel.add(btnDownload);
		
		rangeCombo.addActionListener(e -> {
			btnPause.setSelected(false);
			setRange(xAxis, rangeCombo.getSelectedIndex());
		});
		
		try {
			this.currentType = ChartType.valueOf(appProp.getProperty(DialogAppSettings.PROP_CHARTS_START));
		} catch (Exception e) {
			this.currentType = ChartType.INT_TEMP;
		}
		typeCombo.setSelectedItem(currentType);

		initDataSet(plot.getRangeAxis(), dataset, model, ind);

		NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
		NumberFormat df = NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		yAxis.setNumberFormatOverride(df);

		getRootPane().registerKeyboardAction(e -> {
			int selected = rangeCombo.getSelectedIndex();
			rangeCombo.setSelectedIndex(++selected >= rangeCombo.getItemCount() ? 0 : selected);
		} , KeyStroke.getKeyStroke(KeyEvent.VK_R, MainView.SHORTCUT_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		getRootPane().registerKeyboardAction(e -> {
			btnPause.doClick();
		} , KeyStroke.getKeyStroke(KeyEvent.VK_P, MainView.SHORTCUT_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);

		model.addListener(this);

		setSize(800, 460);
		setLocationRelativeTo(owner);
		setVisible(true);
	}
	
	private static void setRange(ValueAxis xAxis, int selected) {
		if(selected == 1) xAxis.setFixedAutoRange(1000 * 1 * 60);
		else if(selected == 2) xAxis.setFixedAutoRange(1000 * 5 * 60);
		else if(selected == 3) xAxis.setFixedAutoRange(1000 * 15 * 60);
		else if(selected == 4) xAxis.setFixedAutoRange(1000 * 30 * 60);
		else if(selected == 5) xAxis.setFixedAutoRange(1000 * 60 * 60);
		else xAxis.setFixedAutoRange(0);
		xAxis.setAutoRange(true);
	}

	private void initDataSet(ValueAxis yAxis, TimeSeriesCollection dataset, final Devices model, int[] indexes) {
		dataset.removeAllSeries();
		yAxis.setLabel(currentType.yLabel);
		for(int ind: indexes) {
			final ShellyAbstractDevice d = model.get(ind);

			if(currentType.mType == null) {
				TimeSeries s = new TimeSeries(UtilCollecion.getDescName(d));
				dataset.addSeries(s);
				seriesMap.put(ind, new TimeSeries[] {s});
			} else {
				ArrayList<TimeSeries> temp = new ArrayList<>();
				Meters[] meters = d.getMeters();
				if(meters != null) {
					for(int i = 0; i < meters.length; i++) {
						if(meters[i].hasType(currentType.mType)) {
							final String sName = UtilCollecion.getDescName(d, i);
							TimeSeries s = new TimeSeries(sName);
							temp.add(s);
							dataset.addSeries(s);
						}
					}
				}
				if(temp.size() == 0) {
					dataset.addSeries(new TimeSeries(UtilCollecion.getDescName(d))); // legend
				}
				seriesMap.put(ind, temp.toArray(new TimeSeries[temp.size()]));
			}
			update(Devices.EventType.UPDATE, ind);
		}
	}

	@Override
	public void dispose() {
		LOG.trace("closing charts");
		model.removeListener(this);
		super.dispose();
	}

	@Override
	public void update(EventType mesgType, Integer ind) {
		if(mesgType == Devices.EventType.UPDATE) {
			TimeSeries ts[];
			if((ts = seriesMap.get(ind)) != null) {
				SwingUtilities.invokeLater(() -> {
					// System.out.println(ind);
					final ShellyAbstractDevice d = model.get(ind);
					if(d.getStatus() == Status.ON_LINE) {
						final Millisecond timestamp = new Millisecond(new Date(d.getLastTime()));
						Meters[] m;
						if(currentType == ChartType.INT_TEMP && d instanceof InternalTmpHolder) {
							ts[0].addOrUpdate(timestamp, ((InternalTmpHolder)d).getInternalTmp());
						} else if(currentType == ChartType.RSSI) {
							ts[0].addOrUpdate(timestamp, d.getRssi());
						} else if(/*currentType.mType != null &&*/ (m = d.getMeters()) != null) {
							for(int i = 0; i < m.length; i++) {
								if(m[i].hasType(currentType.mType)) {
									ts[i].addOrUpdate(timestamp, m[i].getValue(currentType.mType));
								}
							}
						}
					}
				});
			}
		} else if(mesgType == Devices.EventType.CLEAR) {
			SwingUtilities.invokeLater(() -> dispose());
		}
	}  
}