package it.usna.shellyscan.view.devsettingspanel;

import static it.usna.shellyscan.Main.LABELS;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import it.usna.shellyscan.Main;
import it.usna.shellyscan.model.device.MQTTManager;
import it.usna.shellyscan.model.device.ShellyAbstractDevice;

//https://shelly-api-docs.shelly.cloud/gen2/Components/SystemComponents/Mqtt
//https://shelly-api-docs.shelly.cloud/gen1/#settings
public class PanelMQTTAll extends AbstractSettingsPanel {
	private static final long serialVersionUID = 1L;
	private char pwdEchoChar;
	private JCheckBox chckbxEnabled = new JCheckBox();
	private JTextField textFieldServer;
	private JPasswordField textFieldPwd;
	private JCheckBox chckbxShowPwd;
	private JTextField textFieldUser;
	private JTextField textFieldID;
	private JCheckBox chckbxNoPWD;
	private JCheckBox chckbxDefaultPrefix;
	private List<MQTTManager> mqttModule = new ArrayList<>();

	public PanelMQTTAll(List<ShellyAbstractDevice> devices) {
		super(devices);
		//		this.setSize(800, 800);
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 2, 6));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {10, 0, 30, 0, 0};
		contentPanel.setLayout(gridBagLayout);

		JLabel lblNewLabel = new JLabel(LABELS.getString("dlgSetEnabled"));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		contentPanel.add(lblNewLabel, gbc_lblNewLabel);

		GridBagConstraints gbc_chckbxEnabled = new GridBagConstraints();
		gbc_chckbxEnabled.weightx = 1.0;
		gbc_chckbxEnabled.gridwidth = 4;
		gbc_chckbxEnabled.anchor = GridBagConstraints.WEST;
		gbc_chckbxEnabled.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxEnabled.gridx = 1;
		gbc_chckbxEnabled.gridy = 0;
		chckbxEnabled.setHorizontalAlignment(SwingConstants.LEFT);
		contentPanel.add(chckbxEnabled, gbc_chckbxEnabled);

		JLabel lblNewLabel_1 = new JLabel(LABELS.getString("dlgSetServer"));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		contentPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		textFieldServer = new JTextField();
		GridBagConstraints gbc_textFieldServer = new GridBagConstraints();
		gbc_textFieldServer.gridwidth = 4;
		gbc_textFieldServer.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldServer.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldServer.gridx = 1;
		gbc_textFieldServer.gridy = 1;
		contentPanel.add(textFieldServer, gbc_textFieldServer);
		textFieldServer.setColumns(10);

		JLabel lblNewLabel_8 = new JLabel(LABELS.getString("dlgSetUser"));
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_8.gridx = 0;
		gbc_lblNewLabel_8.gridy = 2;
		contentPanel.add(lblNewLabel_8, gbc_lblNewLabel_8);

		textFieldUser = new JTextField();
		GridBagConstraints gbc_textFieldUser = new GridBagConstraints();
		gbc_textFieldUser.gridwidth = 4;
		gbc_textFieldUser.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldUser.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldUser.gridx = 1;
		gbc_textFieldUser.gridy = 2;
		contentPanel.add(textFieldUser, gbc_textFieldUser);
		textFieldUser.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel(LABELS.getString("labelPassword"));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 3;
		contentPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

		textFieldPwd = new JPasswordField();
		pwdEchoChar = textFieldPwd.getEchoChar();
		GridBagConstraints gbc_textFieldPwd = new GridBagConstraints();
		gbc_textFieldPwd.gridwidth = 4;
		gbc_textFieldPwd.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldPwd.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPwd.gridx = 1;
		gbc_textFieldPwd.gridy = 3;
		contentPanel.add(textFieldPwd, gbc_textFieldPwd);
		textFieldPwd.setColumns(10);

		chckbxShowPwd = new JCheckBox(LABELS.getString("labelShowPwd"));
		chckbxShowPwd.addItemListener(e -> textFieldPwd.setEchoChar((e.getStateChange() == java.awt.event.ItemEvent.SELECTED) ? '\0' : pwdEchoChar));
		setLayout(new BorderLayout(0, 0));
		GridBagConstraints gbc_chckbxSPwd = new GridBagConstraints();
		gbc_chckbxSPwd.gridwidth = 3;
		gbc_chckbxSPwd.anchor = GridBagConstraints.WEST;
		gbc_chckbxSPwd.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSPwd.gridx = 1;
		gbc_chckbxSPwd.gridy = 4;
		contentPanel.add(chckbxShowPwd, gbc_chckbxSPwd);

		chckbxNoPWD = new JCheckBox(LABELS.getString("labelNoPwd"));
		GridBagConstraints gbc_chckbxNoPWD = new GridBagConstraints();
		gbc_chckbxNoPWD.anchor = GridBagConstraints.WEST;
		gbc_chckbxNoPWD.insets = new Insets(0, 10, 5, 0);
		gbc_chckbxNoPWD.gridx = 4;
		gbc_chckbxNoPWD.gridy = 4;
		contentPanel.add(chckbxNoPWD, gbc_chckbxNoPWD);

		JLabel lblNewLabel_3 = new JLabel(LABELS.getString("dlgSetMqttId"));
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 5;
		contentPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);

		textFieldID = new JTextField();
		GridBagConstraints gbc_textFieldID = new GridBagConstraints();
		gbc_textFieldID.gridwidth = 4;
		gbc_textFieldID.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldID.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldID.gridx = 1;
		gbc_textFieldID.gridy = 5;
		contentPanel.add(textFieldID, gbc_textFieldID);
		textFieldID.setColumns(10);
		
		chckbxDefaultPrefix = new JCheckBox(LABELS.getString("dlgSetMqttIdDefault"));
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 1;
		gbc_chckbxNewCheckBox.gridy = 6;
		contentPanel.add(chckbxDefaultPrefix, gbc_chckbxNewCheckBox);

		JLabel lblNewLabel_12 = new JLabel(LABELS.getString("dlgSetMsgMqttReboot"));
		lblNewLabel_12.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_12 = new GridBagConstraints();
		gbc_lblNewLabel_12.weighty = 1.0;
		gbc_lblNewLabel_12.anchor = GridBagConstraints.NORTH;
		gbc_lblNewLabel_12.gridwidth = 5;
		gbc_lblNewLabel_12.gridx = 0;
		gbc_lblNewLabel_12.gridy = 7;
		contentPanel.add(lblNewLabel_12, gbc_lblNewLabel_12);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(12);
		scrollPane.setViewportView(contentPanel);
		add(scrollPane, BorderLayout.CENTER);

		chckbxDefaultPrefix.addItemListener(event -> textFieldID.setEnabled(event.getStateChange() != java.awt.event.ItemEvent.SELECTED  && devices.size() == 1));
		chckbxEnabled.addItemListener(event -> setEnabledMQTT(event.getStateChange() == java.awt.event.ItemEvent.SELECTED, devices.size() == 1));
		chckbxNoPWD.addItemListener(event -> setPasswordRequired(event.getStateChange() == java.awt.event.ItemEvent.DESELECTED));
	}

	private void setPasswordRequired(boolean pwdRequired) {
		textFieldPwd.setEnabled(pwdRequired);
		textFieldUser.setEnabled(pwdRequired);
		chckbxShowPwd.setEnabled(pwdRequired);
	}

	private void setEnabledMQTT(boolean enabled, boolean single) {
		textFieldServer.setEnabled(enabled);
		textFieldUser.setEnabled(enabled);
		textFieldPwd.setEnabled(enabled);
		chckbxShowPwd.setEnabled(enabled);
		chckbxNoPWD.setEnabled(enabled);
		textFieldID.setEnabled(enabled && chckbxDefaultPrefix.isSelected() == false && single);
		chckbxDefaultPrefix.setEnabled(enabled);

		setPasswordRequired(enabled && chckbxNoPWD.isSelected() == false);
	}

	@Override
	public String showing() {
		mqttModule.clear();
		ShellyAbstractDevice d = null;
		try {
			setEnabledMQTT(false, false); // disable while checking
			boolean enabledGlobal = false;
			String serverGlobal = "";
			String userGlobal = "";
			String idGlobal = "";
			boolean first = true;
			for(int i = 0; i < devices.size(); i++) {
				if(Thread.interrupted() == false) {
					d = devices.get(i);
					MQTTManager mqttm = d.getMQTTManager();
					mqttModule.add(mqttm);
					boolean enabled = mqttm.isEnabled();
					String server = mqttm.getServer();
					String user = mqttm.getUser();
					String id = mqttm.getPrefix();
					if(first) {
						enabledGlobal = enabled;
						serverGlobal = server;
						userGlobal = user;
						idGlobal = id;
						first = false;
					} else {
						if(enabled != enabledGlobal) enabledGlobal = false;
						if(server.equals(serverGlobal) == false) serverGlobal = "";
						if(user.equals(userGlobal) == false) userGlobal = "";
						if(id.equals(idGlobal) == false) idGlobal = "";
					}
				}
			}
			chckbxEnabled.setSelected(enabledGlobal);
			textFieldServer.setText(serverGlobal);
			textFieldUser.setText(userGlobal);
			textFieldID.setText(idGlobal);

			setEnabledMQTT(enabledGlobal, devices.size() == 1);
			return null;
		} catch (IOException | RuntimeException e) {
			return getExtendedName(d) + ": " + e.getMessage();
		}
	}

	@Override
	public String apply() {
		final boolean enabled = chckbxEnabled.isSelected();
		final String server = textFieldServer.getText().trim();
		String user = textFieldUser.getText().trim();
		String pwd = new String(textFieldPwd.getPassword()).trim();
		if(enabled) {
			if(chckbxNoPWD.isSelected()) {
				user = pwd = null;
			}
			// Validation
			if(server.isEmpty() || (chckbxNoPWD.isSelected() == false && (user.isEmpty() || pwd.isEmpty()))) {
				throw new IllegalArgumentException(Main.LABELS.getString("dlgSetMsgMqttServer"));
			}
		}
		String res = "<html>";
		for(int i=0; i < devices.size(); i++) {
			String msg;
			if(enabled) {
				String prefix ;
				if(chckbxDefaultPrefix.isSelected()) {
					prefix = null;
				} else if(devices.size() > 1) {
					prefix = "";
				} else {
					prefix = textFieldID.getText();
				}
				msg = mqttModule.get(i).set(server, user, pwd, prefix);
			} else {
				msg = mqttModule.get(i).disable();
			}
			if(msg != null) {
				res += String.format(LABELS.getString("dlgSetMultiMsgFail"), devices.get(i).getHostname()) + " (" + msg + ")<br>";
			} else {
				res += String.format(LABELS.getString("dlgSetMultiMsgOk"), devices.get(i).getHostname()) + "<br>";
			}
		}
		showing();
		return res;
	}
}