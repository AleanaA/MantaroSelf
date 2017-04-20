package net.kodehawa.mantaroself.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.kodehawa.mantaroself.MantaroInfo;
import us.monoid.web.Resty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utils {
	public static ObjectMapper XML_MAPPER = new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	/**
	 * Capitalizes the first letter of a string.
	 *
	 * @param s the string to capitalize
	 * @return A string with the first letter capitalized.
	 */
	public static String capitalize(String s) {
		if (s.length() == 0) return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}

	public static String getDurationMinutes(long length) {
		return String.format("%d:%02d minutes",
			TimeUnit.MILLISECONDS.toMinutes(length),
			TimeUnit.MILLISECONDS.toSeconds(length) -
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length))
		);
	}

	/**
	 * Get a data failure response, place in its own method due to redundancy
	 *
	 * @param url           The URL from which the data fetch failed
	 * @param servicePrefix The prefix from a specific service
	 * @return The formatted response string
	 */
	private static String getFetchDataFailureResponse(String url, String servicePrefix) {
		StringBuilder response = new StringBuilder();
		if (servicePrefix != null) response.append("[").append(servicePrefix).append("]");
		else response.append("\u274C");
		return response.append(" ").append("Hmm, seems like I can't retrieve data from ").append(url).toString();
	}

	public static Iterable<String> iterate(Pattern pattern, String string) {
		return () -> {
			Matcher matcher = pattern.matcher(string);
			return new Iterator<String>() {
				@Override
				public boolean hasNext() {
					return matcher.find();
				}

				@Override
				public String next() {
					return matcher.group();
				}
			};
		};
	}

	public static String paste(String toSend) {
		try {
			String pasteToken = Unirest.post("https://hastebin.com/documents")
				.header("User-Agent", "MantaroSelf v" + MantaroInfo.VERSION)
				.header("Content-Type", "text/plain")
				.body(toSend)
				.asJson()
				.getBody()
				.getObject()
				.getString("key");
			return "https://hastebin.com/" + pasteToken;
		} catch (UnirestException e) {
			log.warn("Error on Hastebin Paste:", e);
			return null;
		}
	}

	public static Comparator<String> randomOrder() {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		int x = r.nextInt(), y = r.nextInt();
		boolean b = r.nextBoolean();
		return Comparator.comparingInt((String s) -> s.hashCode() ^ x)
			.thenComparingInt(s -> s.length() ^ y)
			.thenComparing(b ? Comparator.naturalOrder() : Comparator.reverseOrder());
	}

	public static String toPrettyJson(String jsonString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElement = jsonParser.parse(jsonString);
		return gson.toJson(jsonElement);
	}

	@SneakyThrows
	private static String urlEncodeUTF8(String s) {
		return URLEncoder.encode(s, "UTF-8");
	}

	public static String urlEncodeUTF8(Map<?, ?> map) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(String.format("%s=%s",
				urlEncodeUTF8(entry.getKey().toString()),
				urlEncodeUTF8(entry.getValue().toString())
			));
		}
		return sb.toString();
	}

	/**
	 * Fetches an Object from any given URL. Uses vanilla Java methods.
	 * Can retrieve text, JSON Objects, XML and probably more.
	 *
	 * @param url   The URL to get the object from.
	 * @return The object as a parsed UTF-8 string.
	 */
	public static String wget(String url) {
		String webObject = null;
		try {
			URL ur1 = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) ur1.openConnection();
			conn.setRequestProperty("User-Agent", "Mantaro");
			InputStream ism = conn.getInputStream();
			webObject = CharStreams.toString(new InputStreamReader(ism, Charsets.UTF_8));
		} catch (Exception e) {
			if (e instanceof java.io.FileNotFoundException) return null;
			log.warn(getFetchDataFailureResponse(url, null), e);
		}

		return webObject;
	}

	/**
	 * Same than above, but using resty. Way easier tbh.
	 *
	 * @param url   The URL to get the object from.
	 * @return The object as a parsed string.
	 */
	public static String wgetResty(String url) {
		String url2 = null;
		Resty resty = new Resty().identifyAsMozilla();
		try {
			url2 = resty.text(url).toString();
		} catch (IOException e) {
			log.warn(getFetchDataFailureResponse(url, "Resty"), e);
		}

		return url2;
	}
}