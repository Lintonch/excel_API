package in.psabdp.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.psabdp.service.ExcelProcessorService;

@RestController
@RequestMapping("/api/excel")
public class ExcelProcessorController {

    private final ExcelProcessorService excelProcessorService;

    @Autowired
    public ExcelProcessorController(ExcelProcessorService excelProcessorService) {
        this.excelProcessorService = excelProcessorService;
    }

    // ✅ API 1: Get pass/fail count from the latest 2 Excel files
    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentExcelData() {
        try {
            return ResponseEntity.ok(excelProcessorService.processRecentExcelFiles());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // ✅ API 2: Get available dates for dropdown
    @GetMapping("/available-dates")
    public ResponseEntity<List<String>> getAvailableDates() {
        try {
            return ResponseEntity.ok(excelProcessorService.getAvailableDates());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // ✅ API 3: Get pass/fail count for a selected date
    @GetMapping("/data-by-date")
    public ResponseEntity<Map<String, Integer>> getDataByDate(@RequestParam("date") String date) {
        try {
            Map<String, Integer> data = excelProcessorService.getDataByDate(date);
            return data.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}