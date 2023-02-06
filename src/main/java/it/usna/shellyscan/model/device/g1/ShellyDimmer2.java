package it.usna.shellyscan.model.device.g1;

import java.net.InetAddress;

public class ShellyDimmer2 extends ShellyDimmer {
	public final static String ID = "SHDM-2";

	public ShellyDimmer2(InetAddress address, String hostname) {
		super(address, hostname);
	}
	
	@Override
	public String getTypeName() {
		return "Shelly Dimmer 2";
	}
	
	@Override
	public String getTypeID() {
		return ID;
	}
}