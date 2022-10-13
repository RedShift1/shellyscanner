package it.usna.shellyscan.model.device.g2.modules;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.usna.shellyscan.model.device.g2.AbstractG2Device;
import it.usna.shellyscan.model.device.modules.RelayInterface;

/**
 * Used by 1+
 */
public class Relay implements RelayInterface {
	private final AbstractG2Device parent;
	private final int index;
	private String name;
	private boolean isOn;
	private String source;
	
	public Relay(AbstractG2Device parent, int index) {
		this.parent = parent;
		this.index = index;
	}
	
	public void fillSettings(JsonNode configuration) {
		name = configuration.get("name").asText("");
	}
	
	public void fillStatus(JsonNode relay) throws IOException {
//		JsonNode relay = parent.getJSON("/relay/" + index);
		isOn = relay.get("output").asBoolean();
		source = relay.get("source").asText("-");
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean toggle() throws IOException {
//		final JsonNode relay = parent.getJSON("/rpc/Switch.Toggle?id=" + index);
		final JsonNode relay = parent.getJSON("/relay/" + index + "?turn=toggle");
		isOn = relay.get("ison").asBoolean();
		source = relay.get("source").asText("-");
		return isOn;
	}
	
	@Override
	public void change(boolean on) throws IOException {
		final JsonNode relay = parent.getJSON("/relay/" + index + "?turn=" + (on ? "on" : "off"));
		isOn = relay.get("ison").asBoolean();
		source = relay.get("source").asText("-");
	}
	
	@Override
	public boolean isOn() {
		return isOn;
	}
	
	@Override
	public String getLastSource() {
		return source;
	}
	
	public void restore(JsonNode config, ArrayList<String> errors) throws IOException {
		JsonNodeFactory factory = new JsonNodeFactory(false);
		ObjectNode out = factory.objectNode();
		out.put("id", index);

		ObjectNode input = (ObjectNode)config.get("input:" + index).deepCopy();
		input.remove("id");
		out.set("config", input);
		errors.add(parent.postCommand("Input.SetConfig", out));
		
		ObjectNode sw = (ObjectNode)config.get("switch:" + index).deepCopy();
		sw.remove("id");
		out.set("config", sw);
		errors.add(parent.postCommand("Switch.SetConfig", out));
	}
	
	@Override
	public String getLabel() {
		return name.length() > 0 ? name : parent.getName();
	}
	
	@Override
	public String toString() {
		return getLabel() + "-" + (isOn ? "ON" : "OFF");
	}
}
