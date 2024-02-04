package caeruleusTait.world.preview.backend.storage;

import caeruleusTait.world.preview.WorldPreview;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public interface PreviewStorageCacheManager {

    PreviewStorage loadPreviewStorage(long seed, int yMin, int yMax);

    void storePreviewStorage(long seed, PreviewStorage storage);

    Path cacheDir();

    default void clearCache() {
        try (var stream = Files.walk(cacheDir())) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void writeCacheFile(PreviewStorage storage, Path outFile) {
        WorldPreview.LOGGER.info("Writing preview data to {}", outFile);

        ZipEntry entry = new ZipEntry("bin");
        try (
                FileOutputStream fos = new FileOutputStream(outFile.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos);
        ) {
            zos.putNextEntry(entry);
            ObjectOutputStream oos = new ObjectOutputStream(zos);
            oos.writeObject(storage);
            zos.closeEntry();
        } catch (IOException e) {
            WorldPreview.LOGGER.error("Failed to write cached preview data to {}", outFile);
            e.printStackTrace();
        }
    }

    default PreviewStorage readCacheFile(int yMin, int yMax, Path inFile) {
        if (!Files.exists(inFile)) {
            return new PreviewStorage(yMin, yMax);
        }

        WorldPreview.LOGGER.info("Reading preview data from {}", inFile);

        try (
                FileInputStream fis = new FileInputStream(inFile.toFile());
                ZipInputStream zis = new ZipInputStream(fis);
        ) {
            zis.getNextEntry();
            ObjectInputStream ois = new ObjectInputStream(zis);
            return (PreviewStorage) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            WorldPreview.LOGGER.error("Failed to read cached preview data from {}", inFile);
            e.printStackTrace();
            return new PreviewStorage(yMin, yMax);
        }
    }

}
