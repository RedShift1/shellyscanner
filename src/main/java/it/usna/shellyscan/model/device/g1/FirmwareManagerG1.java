package it.usna.shellyscan.model.device.g1;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import it.usna.shellyscan.model.device.BatteryDeviceInterface;
import it.usna.shellyscan.model.device.FirmwareManager;

public class FirmwareManagerG1 implements FirmwareManager {
	private final static String STATUS_UPDATING = "updating";

	private final AbstractG1Device d;
	private String current;
	private String stable;
	private String beta;
	private boolean updating;
	private boolean valid;
	
	public FirmwareManagerG1(AbstractG1Device d) throws IOException {
		this.d = d;
		try {
			init();
		} catch(IOException e) {
			JsonNode shelly;
			if(d instanceof BatteryDeviceInterface && (shelly = ((BatteryDeviceInterface)d).getStoredJSON("/shelly")) != null) {
				current = shelly.path("fw").asText();
			} else {
				throw e;
			}
		}
	}
	
	private void init() throws IOException {
		valid = false;
		JsonNode node = d.getJSON("/ota");
		updating = STATUS_UPDATING.equals(node.get("status").asText());
		current = node.get("old_version").asText();
		stable = node.get("has_update").asBoolean() ? node.get("new_version").asText() : null;
		boolean hasBeta = node.has("beta_version") && node.get("beta_version").asText().equals(current) == false;
		beta = hasBeta ? node.get("beta_version").asText() : null;
		valid = true;
	}

	@Override
	public void chech() {
		d.sendCommand("/ota/check");
		try {
			init();
		} catch (IOException e) {}
	}
	
	@Override
	public String current() {
		return current;
	}
	
	@Override
	public String newBeta() {
		return beta;
	}
	
	@Override
	public String newStable() {
		return stable;
	}
	
	@Override
	public String update(boolean stable) {
		updating = true;
		return d.sendCommand(stable ? "/ota?update=true" : "/ota?beta=true");
	}
	
	@Override
	public boolean upadating() {
		return updating;
	}
	
	@Override
	public boolean isValid() {
		return valid;
	}
}