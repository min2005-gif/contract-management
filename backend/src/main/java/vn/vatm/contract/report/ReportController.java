package vn.vatm.contract.report;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vatm.contract.report.ReportService.ExportFile;

/** Consolidated reporting endpoints for TCT/management (US3); see contracts/openapi.yaml. */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

  private final ReportService service;

  public ReportController(ReportService service) {
    this.service = service;
  }

  @GetMapping("/summary")
  public ReportSummary summary() {
    return service.summary();
  }

  @GetMapping("/export")
  public ResponseEntity<Resource> export(@RequestParam String format) {
    ExportFile file = service.export(format);
    ContentDisposition disposition =
        ContentDisposition.attachment().filename(file.filename()).build();
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .contentType(MediaType.parseMediaType(file.contentType()))
        .body(new ByteArrayResource(file.content()));
  }
}
