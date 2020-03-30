package ru.r2cloud.sids;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ParamsHandler implements HttpHandler {

	private Map<String, String> params;

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		StringBuilder builder = new StringBuilder();
		String curLine = null;
		BufferedReader r = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
		while ((curLine = r.readLine()) != null) {
			builder.append(curLine).append("\n");
		}
		params = queryToMap(builder.toString().trim());
		exchange.sendResponseHeaders(200, 0);
	}

	public Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				try {
					result.put(entry[0], URLDecoder.decode(entry[1], "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			} else {
				result.put(entry[0], "");
			}
		}
		return result;
	}

	public Map<String, String> getParams() {
		return params;
	}
}
