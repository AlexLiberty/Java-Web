package itstep.learning.services.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.config.ConfigService;

import java.io.*;
import java.util.UUID;

@Singleton
public class DiskStorageService implements StorageService {
    private final String storagePath;

    @Inject
    public DiskStorageService(ConfigService configService) {
        this.storagePath = configService.getValue("storage.path").getAsString();
    }

    @Override
    public String put(InputStream inputStream, String ext) throws IOException {
        String itemId = UUID.randomUUID() + ext;
        File file = new File(storagePath + itemId);
        try (FileOutputStream writer = new FileOutputStream(file)) {
            byte[] buf = new byte[131072];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                writer.write(buf, 0, len);
            }
        }
        return itemId;
    }

    @Override
    public InputStream get(String itemId) throws IOException {
        return new FileInputStream(storagePath + itemId);
    }
}
