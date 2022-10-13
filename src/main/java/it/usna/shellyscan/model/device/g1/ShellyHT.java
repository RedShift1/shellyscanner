package it.usna.shellyscan.model.device.g1;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import org.apache.hc.client5.http.auth.CredentialsProvider;

import com.fasterxml.jackson.databind.JsonNode;

import it.usna.shellyscan.model.device.Meters;

public class ShellyHT extends AbstractBatteryDevice {
	public final static String ID = "SHHT-1";
	private final static Meters.Type[] SUPPORTED_MEASURES = new Meters.Type[] {Meters.Type.BAT, Meters.Type.T, Meters.Type.H};
	private float temp;
	private int humidity;
	private Meters[] meters;

	public ShellyHT(InetAddress address, CredentialsProvider credentialsProv, JsonNode shelly) throws IOException {
		this(address, credentialsProv);
		this.shelly = shelly;
	}
	
	public ShellyHT(InetAddress address, CredentialsProvider credentialsProv) throws IOException {
		super(address, credentialsProv);
		this.settings = getJSON("/settings");
		fillOnce(settings);
		fillSettings(settings);
		this.status = getJSON("/status");
		fillStatus(status);
		
		meters = new Meters[] {
				new Meters() {
					@Override
					public Type[] getTypes() {
						return SUPPORTED_MEASURES;
					}

					@Override
					public float getValue(Type t) {
						if(t == Meters.Type.BAT) {
							return bat;
						} else if(t == Meters.Type.H) {
							return humidity;
						} else {
							return temp;
						}
					}
				}
		};
	}
	
	@Override
	public String getTypeName() {
		return "Shelly H&T";
	}
	
	@Override
	public String getTypeID() {
		return ID;
	}
	
	@Override
	protected void fillSettings(JsonNode settings) throws IOException {
		super.fillSettings(settings);
		this.settings = settings;
	}
	
	@Override
	protected void fillStatus(JsonNode status) throws IOException {
		super.fillStatus(status);
		this.status = status;
		temp = (float)status.get("tmp").get("tC").doubleValue();
		humidity = status.get("hum").get("value").asInt();
		bat = status.get("bat").get("value").asInt();
	}

	public float getTemp() {
		return temp;
	}
	
	public float getHumidity() {
		return humidity;
	}

	@Override
	public Meters[] getMeters() {
		return meters;
	}

	@Override
	protected void restore(JsonNode settings, ArrayList<String> errors) throws IOException {
		JsonNode sensors = settings.get("sensors");
		errors.add(sendCommand("/settings?" +
				jsonNodeToURLPar(settings, "external_power", "temperature_offset", "humidity_offset") + "&" +
				jsonNodeToURLPar(sensors, "temperature_threshold", "humidity_threshold") + "&" +
				"temperature_units=" + sensors.get("temperature_unit").asText())); // temperature_units vs temperature_unit !!!
	}
}