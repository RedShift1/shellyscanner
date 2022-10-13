package it.usna.shellyscan.model.device.g2;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.usna.shellyscan.model.device.MQTTManager;

//https://shelly-api-docs.shelly.cloud/gen2/Components/SystemComponents/Mqtt
public class MQTTManagerG2 implements MQTTManager {
	private final AbstractG2Device d;
	private boolean enabled;
	private String server;
	private String user;
	private String prefix;
	
	// G2 specific
	private String sslCA;
	private boolean rpcNtf;
	private boolean statusNtf;

	public MQTTManagerG2(AbstractG2Device d) throws IOException {
		this.d = d;
		init();
	}

	private void init( ) throws IOException {
		JsonNode settings = d.getJSON("/rpc/MQTT.GetConfig");
		this.enabled = settings.get("enable").asBoolean();
		this.server = settings.path("server").asText("");
		this.user = settings.path("user").asText("");
		this.prefix = settings.path("topic_prefix").asText("");
		this.sslCA = settings.path("ssl_ca").asText("");
		this.rpcNtf = settings.get("rpc_ntf").asBoolean();
		this.statusNtf = settings.get("status_ntf").asBoolean();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String getServer() {
		return server;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	public String getSslCA() {
		return sslCA;
	}

	public boolean isRpcNtf() {
		return rpcNtf;
	}

	public boolean isStatusNtf() {
		return statusNtf;
	}

	@Override
	public String disable() {
		return d.postCommand("MQTT.SetConfig", "{\"config\": {\"enable\": false}}");
	}

	@Override
	public String set(String server, String user, String pwd, String prefix) {
		JsonNodeFactory factory = new JsonNodeFactory(false);
		ObjectNode pars = factory.objectNode();
		pars.put("enable", true);
		pars.put("server", server);
		pars.put("user", user);
		pars.put("pass", pwd);
		if(prefix == null) {
			pars.putNull("topic_prefix"); // device id is used as topic prefix (default)
		} else if(prefix.isEmpty() == false) {
			pars.put("topic_prefix", prefix); // do not alter if field is left blank
		}
		ObjectNode config = factory.objectNode();
		config.set("config", pars);
		return d.postCommand("MQTT.SetConfig", config);
	}
}
