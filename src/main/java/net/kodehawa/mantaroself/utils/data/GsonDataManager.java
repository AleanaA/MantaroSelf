package net.kodehawa.mantaroself.utils.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

@Slf4j
public class GsonDataManager<T> implements DataManager<T> {
	public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().create(), GSON_UNPRETTY = new GsonBuilder().serializeNulls().create();

	public static Gson gson(boolean pretty) {
		return pretty ? GSON_PRETTY : GSON_UNPRETTY;
	}

	private final Path configPath;
	private final T data;

	@SneakyThrows
	public GsonDataManager(Class<T> clazz, String file, Supplier<T> constructor, boolean exitOnNotFound) {
		this.configPath = Paths.get(file);
		if (!configPath.toFile().exists()) {
			log.info("Could not find file at " + configPath.toFile().getAbsolutePath() + ", creating a new one...");
			if (configPath.toFile().createNewFile()) {
				log.info("Generated new file at " + configPath.toFile().getAbsolutePath() + ".");
				FileIOUtils.write(configPath, GSON_PRETTY.toJson(constructor.get()));
				if (exitOnNotFound) {
					log.info("Please, fill the file with valid properties.");
					System.exit(0);
					throw new AssertionError();
				}
			} else {
				log.warn("Could not create file at " + file);
				System.exit(0);
				throw new AssertionError();
			}
		}

		this.data = GSON_PRETTY.fromJson(FileIOUtils.read(configPath), clazz);
	}

	@Override
	public T get() {
		return data;
	}

	@Override
	@SneakyThrows
	public void save() {
		FileIOUtils.write(configPath, GSON_PRETTY.toJson(data));
	}
}