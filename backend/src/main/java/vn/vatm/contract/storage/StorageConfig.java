package vn.vatm.contract.storage;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Wires the MinIO client for on-prem, S3-compatible attachment storage (T018). */
@Configuration
@Profile("!test")
public class StorageConfig {

  @Bean
  public MinioClient minioClient(
      @Value("${app.storage.minio.endpoint}") String endpoint,
      @Value("${app.storage.minio.access-key}") String accessKey,
      @Value("${app.storage.minio.secret-key}") String secretKey) {
    return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
  }
}
