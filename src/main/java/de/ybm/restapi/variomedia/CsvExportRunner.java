package de.ybm.restapi.variomedia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class CsvExportRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CsvExportRunner.class);
    private final CsvExportService csvExportService;

    @Value("${csv.export.path}")
    private String exportPath;

    @Value("${csv.export.filename}")
    private String exportFilename;

    @Value("${csv.export.maxRows}")
    private int maxRows;

    public CsvExportRunner(CsvExportService csvExportService) {
        this.csvExportService = csvExportService;
    }

    @Override
    public void run(String... args) {
        logger.info("🔄 CSV-Export wird gestartet...");
        exportCsv();
    }

    private void exportCsv() {
        String finalFilename = getNextExportFilename();

        // CSV-Datei generieren (mit maxRows)
        csvExportService.generateCsv(exportPath, finalFilename, maxRows);

        // Datei-Pfad setzen
        Path outputPath = Paths.get(exportPath, finalFilename);
        File outputFile = outputPath.toFile();

        // Verzeichnis erstellen, falls es nicht existiert
        if (!outputFile.getParentFile().exists()) {
            boolean dirsCreated = outputFile.getParentFile().mkdirs();
            if (dirsCreated) {
                logger.info("📁 Verzeichnis '{}' wurde erstellt", outputFile.getParentFile().getAbsolutePath());
            } else {
                logger.error("❌ Konnte Verzeichnis '{}' nicht erstellen", outputFile.getParentFile().getAbsolutePath());
                return;
            }
        }

        // Überprüfen, ob die Datei existiert
        if (outputFile.exists()) {
            logger.info("✅ CSV-Export abgeschlossen! Datei gespeichert unter: {}", outputFile.getAbsolutePath());
        }
    }
    /**
     * Erzeugt den nächsten Dateinamen mit fortlaufender Nummerierung.
     */
    private String getNextExportFilename() {
        int fileNumber = 1;
        String filename;

        do {
            filename = exportFilename.replace(".csv", "_" + fileNumber + ".csv");
            fileNumber++;
        } while (new File(exportPath + filename).exists());

        return filename;
    }
}
