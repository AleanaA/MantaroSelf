package net.kodehawa.mantaroself.data;

import net.kodehawa.mantaroself.utils.data.DataManager;
import net.kodehawa.mantaroself.utils.data.GsonDataManager;

public class MantaroData {
	private static DataManager<Config> config;
	private static DataManager<Data> data;

	public static DataManager<Config> config() {
		if (config == null) config = new GsonDataManager<>(Config.class, "config.json", Config::new, true);
		return config;
	}

	public static DataManager<Data> data() {
		if (data == null) data = new GsonDataManager<>(Data.class, "data.json", Data::new, false);
		return data;
	}
}
