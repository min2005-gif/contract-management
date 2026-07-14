package vn.vatm.contract.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;

/** In-memory {@link StorageService} used by tests ({@code app.storage.type=memory}). */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "memory")
public class InMemoryStorageService implements StorageService {

  private final Map<String, byte[]> store = new ConcurrentHashMap<>();

  @Override
  public String put(String keyHint, byte[] content, String contentType) {
    String key = "attachments/" + UUID.randomUUID() + "-" + (keyHint == null ? "file" : keyHint);
    store.put(key, content);
    return key;
  }

  @Override
  public byte[] get(String objectKey) {
    byte[] data = store.get(objectKey);
    if (data == null) {
      throw new NotFoundException("Không tìm thấy tệp. / Object not found: " + objectKey);
    }
    return data;
  }

  @Override
  public void delete(String objectKey) {
    store.remove(objectKey);
  }
}
