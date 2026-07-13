package vn.vatm.contract.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Component;

/** Renders a {@link ReportSummary} to a PDF document (FR-019). */
@Component
public class PdfExporter {

  public byte[] toPdf(ReportSummary summary) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Document doc = new Document();
      PdfWriter.getInstance(doc, out);
      doc.open();

      doc.add(new Paragraph("Báo cáo hợp đồng tổng hợp - VATM"));
      doc.add(new Paragraph("Tổng số hợp đồng: " + summary.totalContracts()));
      doc.add(new Paragraph("Tổng giá trị: " + summary.totalValue().toPlainString()));
      doc.add(new Paragraph("Sắp hết hạn: " + summary.nearingExpiry()));
      doc.add(new Paragraph("Đang thực hiện: " + summary.inProgress()));
      doc.add(new Paragraph(" "));

      PdfPTable table = new PdfPTable(3);
      table.addCell(headerCell("Đơn vị"));
      table.addCell(headerCell("Số hợp đồng"));
      table.addCell(headerCell("Tổng giá trị"));
      for (UnitBreakdown unit : summary.perUnit()) {
        table.addCell(unit.unitName());
        table.addCell(String.valueOf(unit.contractCount()));
        table.addCell(unit.totalValue().toPlainString());
      }
      doc.add(table);

      doc.close();
      return out.toByteArray();
    } catch (DocumentException | java.io.IOException e) {
      throw new IllegalStateException("Không thể tạo tệp PDF. / Failed to build PDF export.", e);
    }
  }

  private PdfPCell headerCell(String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    return cell;
  }
}
