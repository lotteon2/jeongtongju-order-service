package com.jeontongju.order.util;

import com.jeontongju.order.dto.response.admin.AllSellerSettlementDtoForAdmin;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelWriterUtil {
    public static void createExcelFile(List<AllSellerSettlementDtoForAdmin> allSellerSettlementDtoForAdminList) {
        for(AllSellerSettlementDtoForAdmin allSellerSettlementDtoForAdmin : allSellerSettlementDtoForAdminList){
            System.out.println(allSellerSettlementDtoForAdmin.getSellerId());
        }

        try {
            // 엑셀 워크북 생성
            Workbook workbook = new XSSFWorkbook();

            // 시트 생성
            Sheet sheet = workbook.createSheet("Sheet1");

            // 행 및 셀 생성 및 값 설정
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Hello");

            // 다른 행에 값 추가 예시
            Row row2 = sheet.createRow(1);
            Cell cell2 = row2.createCell(1);
            cell2.setCellValue("World");

            // 파일 경로 설정 (사용자의 다운로드 폴더)
            String filePath = getDownloadsPath() + File.separator + "settlement.xlsx";

            // 파일로 저장
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            // 메모리에서 닫음
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getDownloadsPath() {
        return System.getProperty("user.home") + File.separator + "Downloads";
    }
}
