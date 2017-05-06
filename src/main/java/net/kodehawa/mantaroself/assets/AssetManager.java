package net.kodehawa.mantaroself.assets;

import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.kodehawa.mantaroself.MantaroInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.kodehawa.mantaroself.MantaroInfo.BUILD;

@Slf4j
public class AssetManager {
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static void verify() {
		if (validateRemoteSource()) log.info("On a Release Build.");
		else log.warn("On a Development Build. No Remote Assets!");

		log.info("Verifying Assets...");

		File f = new File("assets");

		boolean exists;
		if ((exists = f.exists()) && !f.isDirectory() && (exists = !f.delete())) {
			log.error("File /assets exists and cannot be deleted. Trying to delete on exit...");
			f.deleteOnExit();
			System.exit(0);
			return;
		}

		if (exists) log.info("Assets folder was found with success.");
		if (!exists) unzipFromRemote();
	}

	private static void checkDir(String dir) throws IOException {
		File f = new File(dir);

		if (!f.isDirectory() && f.mkdirs()) throw new IOException("Could not make Directory");
	}

	@SneakyThrows
	private static void unzipFromRemote() {
		if (!validateRemoteSource()) throw new IllegalStateException("On a development build, Can't fetch assets.");

		log.info("Downloading Assets...");

		ZipInputStream input = new ZipInputStream(
			Unirest.get(MantaroInfo.ASSETS_REMOTE)
				.asBinary().getRawBody()
		);

		log.info("Unzipping to /assets/...");

		Logger zip = LoggerFactory.getLogger("assets.zip");

		ZipEntry entry;
		int count = 0;
		while ((entry = input.getNextEntry()) != null) {
			String name = entry.getName();

			count++;
			if (entry.isDirectory()) {
				checkDir(name);
				zip.info("Folder " + name + "(#" + count + ")");
			} else {
				FileOutputStream stream = new FileOutputStream(name);

				int size = 0;
				for (int c = input.read(); c != -1; c = input.read()) {
					stream.write(c);
					size++;
				}

				zip.info("File " + name + "(" + humanReadableByteCount(size, false) + "; #" + count + ")");
				input.closeEntry();
				stream.close();
			}

		}

		input.close();

		log.info("Remote Assets Folder downloaded and " + count + " files extracted.");
	}

	private static boolean validateRemoteSource() {
		return !BUILD.equals("@" + "build" + "@") && !BUILD.startsWith("DEV");
	}
}
