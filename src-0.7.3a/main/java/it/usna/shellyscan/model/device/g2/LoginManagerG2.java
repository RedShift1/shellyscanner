package it.usna.shellyscan.model.device.g2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import com.fasterxml.jackson.databind.JsonNode;

import it.usna.shellyscan.model.device.LoginManager;

//https://shelly-api-docs.shelly.cloud/gen2/Overview/CommonServices/Shelly#shellysetauth
public class LoginManagerG2 implements LoginManager {
	public static String LOGIN_USER = "admin";
	private final AbstractG2Device d;
	private boolean enabled;
	private String realm;

	public LoginManagerG2(AbstractG2Device d) throws IOException {
		this.d = d;
		init();
	}

	private void init() throws IOException {
		JsonNode shelly = d.getJSON("/shelly");
		this.enabled = shelly.get("auth_en").asBoolean();
		this.realm = shelly.get("id").asText();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String getUser() {
		return LOGIN_USER;
	}

	@Override
	public String disable() {
		String msg = d.postCommand("Shelly.SetAuth", "{\"user\":\"" + LOGIN_USER + "\",\"realm\":\"" + realm + "\",\"ha1\":null}");
		if(msg == null) {
			d.setCredentialsProvider(null);
		}
		return msg;
	}

	@Override
	public String set(String dummy, String pwd) {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(LOGIN_USER, pwd));
		return set(null, pwd, credsProvider);
	}

	public String set(String dummy, String pwd, CredentialsProvider credsProvider) {
		String ha1 = LOGIN_USER + ":" + realm + ":" + pwd;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String encodedhash = bytesToHex(digest.digest(ha1.getBytes(StandardCharsets.UTF_8)));
			String msg = d.postCommand("Shelly.SetAuth", "{\"user\":\"" + LOGIN_USER + "\",\"realm\":\"" + realm + "\",\"ha1\":\"" + encodedhash + "\"}");
			if(msg == null) {
				d.setCredentialsProvider(credsProvider);
			}
			return msg;
		} catch (NoSuchAlgorithmException e) {
			return e.toString();
		}
	}
	
	private static String bytesToHex(byte[] hash) {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : hash) {
	        sb.append(String.format("%02x", b));
	    }
	   return sb.toString();
	}
}