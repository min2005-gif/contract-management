package vn.vatm.contract.attachment;

import java.util.List;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.vatm.contract.attachment.AttachmentService.DownloadedFile;

/** REST endpoints for contract attachments (US1); see contracts/openapi.yaml. */
@RestController
@RequestMapping("/api/v1/contracts/{contractId}/attachments")
public class AttachmentController {

  private final AttachmentService service;

  public AttachmentController(AttachmentService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<AttachmentResponse> upload(
      @PathVariable UUID contractId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "kind", required = false) AttachmentKind kind) {
    return ResponseEntity.status(201).body(service.upload(contractId, file, kind));
  }

  @GetMapping
  public List<AttachmentResponse> list(@PathVariable UUID contractId) {
    return service.list(contractId);
  }

  @GetMapping("/{attachmentId}")
  public ResponseEntity<Resource> download(
      @PathVariable UUID contractId, @PathVariable UUID attachmentId) {
    DownloadedFile file = service.download(contractId, attachmentId);
    ContentDisposition disposition =
        ContentDisposition.attachment().filename(file.filename()).build();
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .contentType(MediaType.parseMediaType(file.contentType()))
        .body(new ByteArrayResource(file.content()));
  }

  @DeleteMapping("/{attachmentId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID contractId, @PathVariable UUID attachmentId) {
    service.delete(contractId, attachmentId);
    return ResponseEntity.noContent().build();
  }
}
