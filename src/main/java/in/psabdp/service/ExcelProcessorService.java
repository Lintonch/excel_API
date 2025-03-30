package in.psabdp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import in.psabdp.repos.ExcelDataRepository;

@Service
public class ExcelProcessorService {

    @Value("${excel.folder.path}")
    private String excelFolderPath;

    @Autowired
    private  ExcelDataRepository excelDataRepository;

//    @Autowired
//    public ExcelProcessorService(ExcelDataRepository excelDataRepository) {
//        this.excelDataRepository = excelDataRepository;
//    }

    // ✅ API 1: Process recent files (Latest 2)
    public List<Map<String, Object>> processRecentExcelFiles() throws IOException {
        List<File> excelFiles = getRecentExcelFiles();
        List<Map<String, Object>> fileResults = new ArrayList<>();

        for (File file : excelFiles) {
            fileResults.add(processExcelFile(file));
        }
        return fileResults;
    }

    // ✅ Fetch latest 2 Excel files
    private List<File> getRecentExcelFiles() throws IOException {
        return Files.list(Paths.get(excelFolderPath))
                .map(Path::toFile)
                .filter(file -> file.getName().startsWith("PSABDP_KRA_KPI") && file.getName().endsWith(".xlsx"))
                .sorted(Comparator.comparingLong(File::lastModified).reversed()) // Sort by last modified
                .limit(2)
                .collect(Collectors.toList());
    }

    // ✅ Extract pass/fail count from Excel file
    private Map<String, Object> processExcelFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        int passCount = 0;
        int failCount = 0;

        for (Row row : sheet) {
            Cell cell = row.getCell(13); // Assuming column index 13 contains Pass/Fail status
            if (cell != null) {
                String value = cell.getStringCellValue();
                if ("PASS".equalsIgnoreCase(value)) passCount++;
                if ("FAIL".equalsIgnoreCase(value)) failCount++;
            }
        }
        workbook.close();

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileName", file.getName());
        fileData.put("passCount", passCount);
        fileData.put("failCount", failCount);
        fileData.put("totalCount", passCount + failCount);
        fileData.put("processedAt", LocalDateTime.now());

        return fileData;
    }

    // ✅ API 2: Get available dates for dropdown
    public List<String> getAvailableDates() throws IOException {
        return Files.list(Paths.get(excelFolderPath))
                .map(Path::toFile)
                .filter(file -> file.getName().startsWith("PSABDP_KRA_KPI") && file.getName().endsWith(".xlsx"))
                .map(file -> extractDateFromFileName(file.getName()))
                .distinct()
                .sorted(Comparator.reverseOrder()) // Sort by recent date
                .collect(Collectors.toList());
    }

    //  API 3: Get pass/fail count for selected date
    public Map<String, Integer> getDataByDate(String date) throws IOException {
        List<File> matchingFiles = Files.list(Paths.get(excelFolderPath))
                .map(Path::toFile)
                .filter(file -> file.getName().contains(date) && file.getName().startsWith("PSABDP_KRA_KPI"))
                .collect(Collectors.toList());

        int passCount = 0;
        int failCount = 0;

        for (File file : matchingFiles) {
            Map<String, Object> counts = processExcelFile(file);
            passCount += (int) counts.get("passCount");
            failCount += (int) counts.get("failCount");
        }

        if (passCount == 0 && failCount == 0) return Collections.emptyMap();

        Map<String, Integer> result = new HashMap<>();
        result.put("passCount", passCount);
        result.put("failCount", failCount);
        result.put("totalCount", passCount + failCount);

        return result;
    }

    // Extract date from file name (Assumes format "PSABDP_KRA_KPI_YYYY-MM-DD_...")
    private String extractDateFromFileName(String fileName) {
        String[] parts = fileName.split("_");
        return parts.length >= 4 ? parts[3] : "Unknown";
    }
}
