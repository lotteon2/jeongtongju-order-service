package com.jeontongju.order.util;

import com.jeontongju.order.dto.response.admin.AllSellerSettlementDtoForAdmin;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class ExcelWriterUtil {

    private static final String SHEET_NAME = "Sheet1";

    public static ResponseEntity<byte[]> createExcelFileResponse(List<AllSellerSettlementDtoForAdmin> allSellerSettlementDtoForAdminList) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            createHeaderRow(sheet);
            createDataRows(sheet, allSellerSettlementDtoForAdminList);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "settlement_" + getCurrentTimestamp() + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
            .body(outputStream.toByteArray());

        } catch (IOException e) {
            log.error("액셀 파일 만드는데 문제가 발생했습니다.", e);
            throw new RuntimeException(e);
        }
    }

    private static void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("셀러아이디");
        headerRow.createCell(1).setCellValue("셀러명");
        headerRow.createCell(2).setCellValue("정산년");
        headerRow.createCell(3).setCellValue("정산월");
        headerRow.createCell(4).setCellValue("총금액");
        headerRow.createCell(5).setCellValue("수수료");
        headerRow.createCell(6).setCellValue("판매액");
    }

    private static void createDataRows(Sheet sheet, List<AllSellerSettlementDtoForAdmin> dataList) {
        int rowNum = 1;
        for (AllSellerSettlementDtoForAdmin dto : dataList) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(dto.getSellerId());
            dataRow.createCell(1).setCellValue(dto.getSellerName());
            dataRow.createCell(2).setCellValue(dto.getSettlementYear());
            dataRow.createCell(3).setCellValue(dto.getSettlementMonth());
            dataRow.createCell(4).setCellValue(dto.getSettlementAmount());
            dataRow.createCell(5).setCellValue(dto.getSettlementCommission());
            dataRow.createCell(6).setCellValue(dto.getSettlementAmount() - dto.getSettlementCommission());
        }
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return dateFormat.format(new Date());
    }
}
