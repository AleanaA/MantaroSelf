package net.kodehawa.mantaroself.data;

/**
 * Yes, I'm well aware of how ugly this class is. I don't care. It works.
 *
 * @author AdrianTodt
 */
public class Config {
	public static class AniList {
		public final String Description = "AniList (anilist.co) is the Service Provider of Anime and Character Commands. You need to register an Application if you want those Commands.";
		public String Client, Secret;
	}

	public static class Bot {
		public final String Description = "General Settings. All the Fields are Required.";
		public String Token, Prefix = "self.";
	}

	public static class Weather {
		public final String Description = "Weather (openweathermap.org) is the Service Provider of the Weather Command. You need to register an Application if you want the Command.";
		public String AppId;
	}

	public AniList AniList = new AniList();
	public Bot Bot = new Bot();
	public Weather Weather = new Weather();

	public String aniListClient() {
		return AniList.Client;
	}

	public String aniListSecret() {
		return AniList.Secret;
	}

	public Config ensureNonNull() {
		if (AniList == null) AniList = new AniList();
		if (Bot == null) Bot = new Bot();
		if (Weather == null) Weather = new Weather();

		return this;
	}

	public String prefix() {
		return Bot.Prefix;
	}

	public String token() {
		return Bot.Token;
	}

	public String weatherAppId() {
		return Weather.AppId;
	}

}
