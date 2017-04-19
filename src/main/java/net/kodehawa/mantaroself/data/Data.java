package net.kodehawa.mantaroself.data;

import java.util.*;

public class Data {
	public static class Quote {
		Date date;
		String userId, guildId, channelId;
		String username, userAvatar, guildName, channelName, content;
	}

	public Map<String, List<String>> custom = new HashMap<>();
	public List<Quote> quotes = new ArrayList<>();
}
