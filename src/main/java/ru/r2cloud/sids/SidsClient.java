package ru.r2cloud.sids;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.TimeZone;

public class SidsClient {

	private final static TimeZone GMT = TimeZone.getTimeZone("GMT");
	private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	private final String url;
	private final int timeout;

	/**
	 * @param url
	 *            - list of urls can be found at http://www.pe0sat.vgnet.nl/decoding/tlm-decoding-software/sids/
	 * @param timeout
	 */
	public SidsClient(String url, int timeout) {
		if (url == null || url.trim().length() == 0) {
			throw new IllegalArgumentException("url cannot be blank");
		}
		this.url = url;
		this.timeout = timeout;
	}

	public void send(Telemetry data) throws IOException, RequestException {
		validate(data);
		StringBuilder urlParameters = new StringBuilder();
		urlParameters.append("noradID=").append(URLEncoder.encode(data.getNoradId(), "UTF-8"));
		urlParameters.append("&source=").append(URLEncoder.encode(data.getCallsign(), "UTF-8"));
		urlParameters.append("&locator=longLat");
		String longitudeValue = String.valueOf(Math.abs(data.getLongitude()));
		String latitudeValue = String.valueOf(Math.abs(data.getLatitude()));
		try {
			if (data.getLongitude() >= 0) {
				longitudeValue = URLEncoder.encode(longitudeValue + "E", "UTF-8");
			} else {
				longitudeValue = URLEncoder.encode(longitudeValue + "W", "UTF-8");
			}
			if (data.getLatitude() >= 0) {
				latitudeValue = URLEncoder.encode(latitudeValue + "N", "UTF-8");
			} else {
				latitudeValue = URLEncoder.encode(latitudeValue + "S", "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("encoding is unsupported", e);
		}
		urlParameters.append("&longitude=").append(longitudeValue);
		urlParameters.append("&latitude=").append(latitudeValue);
		urlParameters.append("&version=1.0");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		sdf.setTimeZone(GMT);
		
		urlParameters.append("&timestamp=").append(URLEncoder.encode(sdf.format(data.getTimestamp()) + "Z", "UTF-8"));
		
		urlParameters.append("&frame=").append(bytesToHex(data.getFrame()));
		byte[] postData = urlParameters.toString().getBytes(StandardCharsets.UTF_8);
		DataOutputStream wr = null;
		HttpURLConnection conn = null;
		try {
			URL obj = new URL(url);
			conn = (HttpURLConnection) obj.openConnection();
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("User-Agent", "sids/1.2 (https://github.com/dernasherbrezon/sids)");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
			conn.setUseCaches(false);
			wr = new DataOutputStream(conn.getOutputStream());
			wr.write(postData);
			wr.close();
			int responseCode = conn.getResponseCode();
			if (responseCode < 200 || responseCode >= 300) {
				throw new RequestException("unable to send telemetry: " + data.getNoradId(), responseCode, convertStreamToString(conn.getInputStream()));
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private static void validate(Telemetry telemetry) {
		if (telemetry.getCallsign() == null || telemetry.getCallsign().trim().length() == 0) {
			throw new IllegalArgumentException("callsign is empty");
		}
		if (telemetry.getFrame() == null || telemetry.getFrame().length == 0) {
			throw new IllegalArgumentException("frame is empty");
		}
		if (telemetry.getNoradId() == null || telemetry.getNoradId().trim().length() == 0) {
			throw new IllegalArgumentException("noradId is empty");
		}
		if (telemetry.getTimestamp() == null) {
			throw new IllegalArgumentException("timestamp is empty");
		}
	}

	private static String convertStreamToString(InputStream is) {
		@SuppressWarnings("resource")
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}
	
}
