package vn.vatm.contract.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/** Renders a {@link ReportSummary} to an .xlsx workbook (FR-019). */
@Component
public class ExcelExporter {

  public byte[] toXlsx(ReportSummary summary) {
    try (Workbook wb = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      CellStyle bold = wb.createCellStyle();
      Font font = wb.createFont();
      font.setBold(true);
      bold.setFont(font);

      Sheet totals = wb.createSheet("Tổng hợp");
      addLabelValue(totals, 0, "Tổng số hợp đồng", summary.totalContracts(), bold);
      addLabelValue(totals, 1, "Tổng giá trị", summary.totalValue().doubleValue(), bold);
      addLabelValue(totals, 2, "Sắp hết hạn", summary.nearingExpiry(), bold);
      addLabelValue(totals, 3, "Đang thực hiện", summary.inProgress(), bold);

      Sheet byUnit = wb.createSheet("Theo đơn vị");
      Row header = byUnit.createRow(0);
      writeCell(header, 0, "Đơn vị", bold);
      writeCell(header, 1, "Số hợp đồng", bold);
      writeCell(header, 2, "Tổng giá trị", bold);
      int r = 1;
      for (UnitBreakdown unit : summary.perUnit()) {
        Row row = byUnit.createRow(r++);
        row.createCell(0).setCellValue(unit.unitName());
        row.createCell(1).setCellValue(unit.contractCount());
        row.createCell(2).setCellValue(unit.totalValue().doubleValue());
      }

      wb.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Không thể tạo tệp Excel. / Failed to build Excel export.", e);
    }
  }

  private void addLabelValue(Sheet sheet, int rowIdx, String label, double value, CellStyle bold) {
    Row row = sheet.createRow(rowIdx);
    writeCell(row, 0, label, bold);
    row.createCell(1).setCellValue(value);
  }

  private void writeCell(Row row, int col, String text, CellStyle style) {
    Cell cell = row.createCell(col);
    cell.setCellValue(text);
    cell.setCellStyle(style);
  }
}
