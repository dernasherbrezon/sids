package ru.r2cloud.sids;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SidsClientTest {

	private HttpServer server;
	private String host = "localhost";
	private int port = 8000;
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidFrame() throws Exception {
		Telemetry data = createData();
		data.setFrame(new byte[0]);
		SidsClient client = new SidsClient("http://" + host + ":" + port + "/sids", 5000);
		client.send(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTimestamp() throws Exception {
		Telemetry data = createData();
		data.setTimestamp(null);
		SidsClient client = new SidsClient("http://" + host + ":" + port + "/sids", 5000);
		client.send(data);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidNoradid() throws Exception {
		Telemetry data = createData();
		data.setNoradId("");
		SidsClient client = new SidsClient("http://" + host + ":" + port + "/sids", 5000);
		client.send(data);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCallsign() throws Exception {
		Telemetry data = createData();
		data.setCallsign("");
		SidsClient client = new SidsClient("http://" + host + ":" + port + "/sids", 5000);
		client.send(data);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidArgument() {
		new SidsClient(null, 5000);
	}

	@Test(expected = IOException.class)
	public void testInvalidResponseCode() throws Exception {
		server.createContext("/sids", new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {
				exchange.sendResponseHeaders(404, 0);
			}
		});
		SidsClient client = new SidsClient("http://" + host + ":" + port + "/sids", 5000);
		client.send(createData());
	}

	@Test
	public void testSuccess() throws Exception {
		ParamsHandler handler = new ParamsHandler();
		server.createContext("/sids", handler);
		SidsClient client = new SidsClient("http://" + host + ":" + port + "/sids", 5000);
		Telemetry telemetry = createData();
		client.send(telemetry);
		assertEquals("CAFE01", handler.getParams().get("frame"));
		assertEquals(telemetry.getCallsign(), handler.getParams().get("source"));
	}

	private static Telemetry createData() {
		Telemetry data = new Telemetry();
		//space is for URLEncoding test
		data.setCallsign(UUID.randomUUID().toString() + " test");
		data.setFrame(new byte[] { (byte) 0xca, (byte) 0xfe, 0x01 });
		data.setLatitude(1.0);
		data.setLongitude(1.0);
		data.setNoradId(UUID.randomUUID().toString());
		data.setTimestamp(new Date());
		return data;
	}

	@Before
	public void start() throws Exception {
		server = HttpServer.create(new InetSocketAddress(host, port), 0);
		server.start();
	}

	@After
	public void stop() {
		if (server != null) {
			server.stop(0);
		}
	}
}
