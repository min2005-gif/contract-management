package vn.vatm.contract.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** MinIO-backed {@link StorageService} for production/dev (T018). */
@Service
@Profile("!test")
public class MinioStorageService implements StorageService {

  private final MinioClient client;
  private final String bucket;

  public MinioStorageService(
      MinioClient client, @Value("${app.storage.minio.bucket}") String bucket) {
    this.client = client;
    this.bucket = bucket;
  }

  @Override
  public String put(String keyHint, byte[] content, String contentType) {
    try {
      ensureBucket();
      String key = "attachments/" + UUID.randomUUID() + "-" + sanitize(keyHint);
      try (InputStream in = new ByteArrayInputStream(content)) {
        client.putObject(
            PutObjectArgs.builder().bucket(bucket).object(key).stream(in, content.length, -1)
                .contentType(contentType)
                .build());
      }
      return key;
    } catch (Exception e) {
      throw new StorageException("Không thể lưu tệp đính kèm. / Failed to store attachment.", e);
    }
  }

  @Override
  public byte[] get(String objectKey) {
    try (InputStream in =
        client.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build())) {
      return in.readAllBytes();
    } catch (Exception e) {
      throw new StorageException("Không thể đọc tệp đính kèm. / Failed to read attachment.", e);
    }
  }

  @Override
  public void delete(String objectKey) {
    try {
      client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new StorageException("Không thể xóa tệp đính kèm. / Failed to delete attachment.", e);
    }
  }

  private void ensureBucket() throws Exception {
    boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    if (!exists) {
      client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }

  private String sanitize(String name) {
    return name == null ? "file" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  /** Wraps storage failures. */
  public static class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
