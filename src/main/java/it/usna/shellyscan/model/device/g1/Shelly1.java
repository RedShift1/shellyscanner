package it.usna.shellyscan.model.device.g1;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.auth.CredentialsProvider;

import com.fasterxml.jackson.databind.JsonNode;

import it.usna.shellyscan.model.Devices;
import it.usna.shellyscan.model.device.Meters;
import it.usna.shellyscan.model.device.g1.modules.Relay;
import it.usna.shellyscan.model.device.modules.RelayCommander;
import it.usna.shellyscan.model.device.modules.RelayInterface;

public class Shelly1 extends AbstractG1Device implements RelayCommander {
	public final static String ID = "SHSW-1";
	private final static Meters.Type[] SUPPORTED_MEASURES_H = new Meters.Type[] {Meters.Type.T, Meters.Type.H};
	private final static Meters.Type[] MEASURES_EXT_SWITCH = new Meters.Type[] {Meters.Type.EXS};
	private Relay relay = new Relay(this, 0);
	private float extT0, extT1, extT2;// = new float[3];
	private int humidity;
	private int extSwitchStatus;
	private boolean extSwitchRev;
	private Meters[] meters = null;
	
	public Shelly1(InetAddress address, CredentialsProvider credentialsProv) throws IOException {
		super(address, credentialsProv);
		JsonNode settings = getJSON("/settings");
		fillOnce(settings);
		fillSettings(settings);
		JsonNode status = getJSON("/status");
		fillStatus(getJSON("/status"));

		JsonNode extTNode;
		if(settings.path("ext_humidity").size() > 0) {
			meters = new Meters[] {
					new Meters() {
						@Override
						public Type[] getTypes() {
							return SUPPORTED_MEASURES_H;
						}

						@Override
						public float getValue(Type t) {
							return (t == Type.T) ? extT0 : humidity;
						}
					}
			};
		} else if((extTNode = settings.path("ext_temperature")).size() > 0) {
			final ArrayList<Meters.Type> tt = new ArrayList<>(3);
			if(extTNode.has("0")) tt.add(Meters.Type.T);
			if(extTNode.has("1")) tt.add(Meters.Type.TX1);
			if(extTNode.has("2")) tt.add(Meters.Type.TX2);
			final Meters.Type[] mTypes = tt.toArray(new Meters.Type[tt.size()]);
			meters = new Meters[] {
					new Meters() {
						@Override
						public Type[] getTypes() {
							return mTypes;
						}

						@Override
						public float getValue(Type t) {
							if(t == Type.T) {
								return extT0;
							} else if(t == Type.TX1) {
								return extT1;
							} else {
								return extT2;
							}
						}
					}//ext_switch_reverse
			};
		} else if(status.path("ext_switch").size() > 0) { // status
			meters = new Meters[] {
					new Meters() {
						@Override
						public Type[] getTypes() {
							return MEASURES_EXT_SWITCH;
						}

						@Override
						public float getValue(Type t) {
							if(extSwitchRev) {
								return extSwitchStatus == 0 ? 1 : 0;
							} else {
								return extSwitchStatus;
							}
						}
					}
			};
		}
	}

	@Override
	public String getTypeName() {
		return "Shelly 1";
	}

	@Override
	public String getTypeID() {
		return ID;
	}

	@Override
	public Meters[] getMeters() {
		return meters;
	}

	@Override
	public RelayInterface getRelay(int index) {
		return relay;
	}

	@Override
	public RelayInterface[] getRelays() {
		return new RelayInterface[] {relay};
	}

	@Override
	protected void fillSettings(JsonNode settings) throws IOException {
		super.fillSettings(settings);
		relay.fillSettings(settings.get("relays").get(0));
		extSwitchRev = settings.path("ext_switch_reverse").asBoolean();
	}

	@Override
	protected void fillStatus(JsonNode status) throws IOException {
		super.fillStatus(status);
		relay.fillStatus(status.get("relays").get(0), status.get("inputs").get(0));

		JsonNode extTNode = status.path("ext_temperature");
		if(extTNode.size() > 0) {
			extT0 = (float)extTNode.path("0").path("tC").asDouble();
			extT1 = (float)extTNode.path("1").path("tC").asDouble();
			extT2 = (float)extTNode.path("2").path("tC").asDouble();
		}
		JsonNode extHNode = status.path("ext_humidity");
		if(extHNode.size() > 0) {
			humidity = extHNode.path("0").path("hum").asInt();
		}
		
		JsonNode extSwitchNode = status.path("ext_switch");
		if(extSwitchNode.size() > 0) {
			extSwitchStatus = extSwitchNode.path("0").path("input").asInt();
		}
	}

	@Override
	protected void restore(JsonNode settings, ArrayList<String> errors) throws IOException, InterruptedException {
		errors.add(sendCommand("/settings?" + jsonNodeToURLPar(settings, "longpush_time", "factory_reset_from_switch",
				"wifirecovery_reboot_enabled", "ext_switch_enable", "ext_switch_reverse"/*, "eco_mode_enabled"*/) +
				"&ext_sensors_temperature_unit=" + settings.path("ext_sensors").path("temperature_unit")));
		
		TimeUnit.MILLISECONDS.sleep(Devices.MULTI_QUERY_DELAY);
		errors.add(relay.restore(settings.get("relays").get(0)));

//		errors.add(sendCommand("/settings?ext_sensors_temperature_unit=" + settings.path("ext_sensors").path("temperature_unit")));
		for(int i = 0; i < 3; i++) {
			JsonNode extT = settings.path("ext_temperature").path(i + "");
//			if(extT.isEmpty() == false) {
			TimeUnit.MILLISECONDS.sleep(Devices.MULTI_QUERY_DELAY);
			errors.add(sendCommand("/settings/ext_temperature/" + i + "?" + jsonEntryIteratorToURLPar(extT.fields())));
//			}
		}
		TimeUnit.MILLISECONDS.sleep(Devices.MULTI_QUERY_DELAY);
		errors.add(sendCommand("/settings/ext_humidity/0?" + jsonEntryIteratorToURLPar(settings.path("ext_humidity").path("0").fields())));
		TimeUnit.MILLISECONDS.sleep(Devices.MULTI_QUERY_DELAY);
		errors.add(sendCommand("/settings/ext_switch/0?" + jsonEntryIteratorToURLPar(settings.path("ext_switch").path("0").fields())));
	}

	@Override
	public String toString() {
		return super.toString() + " Relay: " + relay;
	}
}

/*
settings:

"ext_sensors" : {
  "temperature_unit" : "C"
},
"ext_temperature" : {
  "0" : {
    "overtemp_threshold_tC" : 0.0,
    "overtemp_threshold_tF" : 32.0,
    "undertemp_threshold_tC" : 0.0,
    "undertemp_threshold_tF" : 32.0,
    "overtemp_act" : "disabled",
    "undertemp_act" : "disabled",
    "offset_tC" : 0.0,
    "offset_tF" : 0.0
  }
},
"ext_humidity" : { },


satus:

 "ext_sensors" : {
  "temperature_unit" : "C"
},
"ext_temperature" : {
  "0" : {
    "hwID" : "281863eb2f1901ad",
    "tC" : 22.5,
    "tF" : 72.5
  }
},
"ext_humidity" : { },
*/