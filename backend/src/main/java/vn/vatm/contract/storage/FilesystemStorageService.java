package vn.vatm.contract.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;

/**
 * Filesystem-backed {@link StorageService} ({@code app.storage.type=filesystem}). Used for simple
 * (e.g. free-tier) deployments without an object store. Note: on ephemeral hosts the directory is
 * wiped on redeploy — swap in MinIO/S3 for durable storage.
 */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "filesystem")
public class FilesystemStorageService implements StorageService {

  private final Path root;

  public FilesystemStorageService(@Value("${app.storage.dir:./attachments}") String dir) {
    this.root = Paths.get(dir);
  }

  @Override
  public String put(String keyHint, byte[] content, String contentType) {
    try {
      Files.createDirectories(root);
      String key = UUID.randomUUID() + "-" + sanitize(keyHint);
      Files.write(root.resolve(key), content);
      return key;
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Không thể lưu tệp đính kèm. / Failed to store attachment.", e);
    }
  }

  @Override
  public byte[] get(String objectKey) {
    Path path = root.resolve(objectKey);
    if (!Files.exists(path)) {
      throw new NotFoundException("Không tìm thấy tệp. / Object not found: " + objectKey);
    }
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new UncheckedIOException("Không thể đọc tệp đính kèm. / Failed to read attachment.", e);
    }
  }

  @Override
  public void delete(String objectKey) {
    try {
      Files.deleteIfExists(root.resolve(objectKey));
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Không thể xóa tệp đính kèm. / Failed to delete attachment.", e);
    }
  }

  private String sanitize(String name) {
    return name == null ? "file" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
