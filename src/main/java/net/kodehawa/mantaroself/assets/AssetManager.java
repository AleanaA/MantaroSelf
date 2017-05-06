package net.kodehawa.mantaroself.assets;

import com.mashape.unirest.http.Unirest;
import lombok.extern.slf4j.Slf4j;
import net.kodehawa.mantaroself.MantaroInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.kodehawa.mantaroself.MantaroInfo.BUILD;

@Slf4j
public class AssetManager {
	public static void verify() {
		log.info("Verifying Assets...");

		if (validateRemoteSource()) log.info("On a Release Build.");
		else log.warn("On a Development Build. No Remote Assets!");

		File f = new File("assets");

		boolean exists;
		if ((exists = f.exists()) && !f.isDirectory() && (exists = !f.delete())) {
			log.error("File /assets exists and cannot be deleted. Trying to delete on exit...");
			f.deleteOnExit();
			System.exit(0);
			return;
		}

		if (exists) log.info("Assets folder was found with success.");
		if (!exists && !unzipFromRemote()) throw new IllegalStateException("No /assets/ folder. Aborting...");
	}

	private static void checkDir(String dir) throws IOException {
		File f = new File(dir);

		if (!f.isDirectory() && f.mkdirs()) throw new IOException("Could not make Directory");
	}

	private static boolean unzipFromRemote() {
		try {
			if (!validateRemoteSource()) {
				throw new IllegalStateException("On a development build, Can't fetch assets.");
			}

			ZipInputStream input = new ZipInputStream(
				Unirest.get(MantaroInfo.ASSETS_REMOTE)
					.asBinary().getRawBody()
			);

			ZipEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				String name = entry.getName();

				log.debug("Unzipping " + name);

				if (entry.isDirectory()) {
					checkDir("/" + name);
				} else {
					FileOutputStream stream = new FileOutputStream("/" + name);

					for (int c = input.read(); c != -1; c = input.read()) {
						stream.write(c);
					}

					input.closeEntry();
					stream.close();
				}

			}

			input.close();
		} catch (Exception e) {
			log.debug("Exception on Unzipping: ", e);
			return false;
		}

		log.info("Remote Assets Folder downloaded and extracted.");
		return true;
	}

	private static boolean validateRemoteSource() {
		return !BUILD.equals("@build@") && !BUILD.startsWith("DEV");
	}
}
