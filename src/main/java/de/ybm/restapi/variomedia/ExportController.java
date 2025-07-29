package de.ybm.restapi.variomedia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    private final CsvExportService csvExportService;

    @Value("${csv.export.path}")
    private String exportPath;

    @Value("${csv.export.filename}")
    private String exportFilenameBase;

    @Value("${csv.export.maxRows}")
    private int maxRows;

    private static final String EXPORT_EXTENSION = ".csv";
    private final AtomicInteger fileCounter = new AtomicInteger(1);

    public ExportController(CsvExportService csvExportService) {
        this.csvExportService = csvExportService;
    }

    @GetMapping("/variomedia")
    public ResponseEntity<Resource> exportVariomediaData() {
        String exportFilename = getNextExportFilename();

        csvExportService.generateCsv(exportPath, exportFilename, maxRows);

        Path filePath = Paths.get(exportPath + exportFilename);

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + exportFilename);
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String getNextExportFilename() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return exportFilenameBase + "_" + timestamp + "_" + fileCounter.getAndIncrement() + EXPORT_EXTENSION;
    }
}