package vn.vatm.contract.storage;

/** Abstraction over attachment binary storage (MinIO in prod, in-memory in tests). */
public interface StorageService {

  /** Stores the given bytes and returns the object key to persist against the attachment. */
  String put(String keyHint, byte[] content, String contentType);

  /** Retrieves stored bytes by key. */
  byte[] get(String objectKey);

  /** Deletes stored bytes by key (no-op if already absent). */
  void delete(String objectKey);
}
