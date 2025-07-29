package de.ybm.restapi.variomedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class CsvExportService {

    private final VariomediaService variomediaService;

    @Value("${csv.export.filename}")
    private String exportFilenameBase;

    public CsvExportService(VariomediaService variomediaService) {
        this.variomediaService = variomediaService;
    }

    /**
     * Generiert CSV-Dateien und erstellt eine neue Datei, wenn das `maxRows`-Limit erreicht ist.
     */
    public void generateCsv(String exportPath, String exportFilename, int maxRows) {
        System.out.println("📂 Starte CSV-Generierung...");

        JsonNode customers = variomediaService.getAllCustomers();
        if (customers == null || !customers.has("data") || customers.get("data").isEmpty()) {
            System.out.println("Keine Kunden gefunden oder API-Antwort leer!");
            return;
        }

        Set<String> uniqueRecords = new HashSet<>();
        int totalRecords = 0;
        int fileCount = 1;

        BufferedWriter fileWriter = null;
        CSVWriter csvWriter = null;

        try {
            String currentFileName = getNextExportFilename(fileCount);
            fileWriter = new BufferedWriter(new FileWriter(exportPath + currentFileName, false));
            csvWriter = new CSVWriter(fileWriter);

            // CSV-Header ohne "Domain"
            csvWriter.writeNext(new String[]{
                    "Kunde ID", "Kunde Name", "Record Type", "FQDN", "Name", "Domain Name", "Data", "TTL", "Tags"
            });
            safeSleep(1000);
            for (JsonNode customer : customers.path("data")) {
                String customerId = customer.path("id").asText();
                String customerName = customer.path("attributes").path("organization").asText();

                System.out.println("✔ Verarbeite Kunde: " + customerId + " (" + customerName + ")");

                JsonNode domains = variomediaService.getDomainsForCustomer(customerId);
                if (domains == null || !domains.has("data") || domains.get("data").isEmpty()) {
                    System.out.println("Kunde " + customerId + " hat KEINE Domains.");
                    continue;
                }

                for (JsonNode domain : domains.path("data")) {
                    String domainName = domain.path("attributes").path("name").asText();
                    if (domainName.isEmpty()) {
                        System.out.println("Kunde " + customerId + " hat eine leere Domain.");
                        continue;
                    }

                    System.out.println("Rufe DNS-Settings für " + domainName + " ab...");
                    JsonNode settings = variomediaService.getDomainSettings(domainName);


                    if (settings == null || !settings.has("data") || settings.get("data").isEmpty()) {
                        System.out.println("Domain " + domainName + " hat ungültige oder leere DNS-Settings.");
                        continue;
                    }

                    for (JsonNode setting : settings.path("data")) {
                        if (processSetting(setting, customerId, customerName, uniqueRecords, csvWriter)) {
                            totalRecords = checkMaxRows(totalRecords, maxRows, fileCount, csvWriter, fileWriter, exportPath);
                        }
                    }

                }
                safeSleep(1000);
            }

            System.out.println("📂 Alle CSV-Dateien generiert!");

        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der CSV-Datei: " + e.getMessage());
        } finally {
            try {
                if (csvWriter != null) csvWriter.close();
                if (fileWriter != null) fileWriter.close();
            } catch (IOException e) {
                System.err.println("Fehler beim Schließen der Datei: " + e.getMessage());
            }
        }
    }

    private boolean processSetting(JsonNode setting, String customerId, String customerName,
                                   Set<String> uniqueRecords, CSVWriter csvWriter) {
        String recordType = setting.path("attributes").path("record_type").asText();
        String fqdn = setting.path("attributes").path("fqdn").asText();
        String name = setting.path("attributes").path("name").asText();
        String domainName = setting.path("attributes").path("domain").asText();
        String data = setting.path("attributes").path("data").asText();
        String ttl = setting.path("attributes").path("ttl").asText();

        List<String> tagsList = new ArrayList<>();
        for (JsonNode tag : setting.path("attributes").path("tags")) {
            tagsList.add(tag.asText());
        }
        String tags = String.join(", ", tagsList);

        if (!recordType.equals("A") && !recordType.equals("AAAA")) return false;
//        if (domainName.isEmpty()) return false;
//        if (data.isEmpty() || data.equals("0.0.0.0") || data.equals("127.0.0.1")) return false;

        String recordKey = customerId + ";" + recordType + ";" + fqdn+ ";" + name + ";" + domainName + ";" + data + ";" + ttl + ";" + tags;
        if (!uniqueRecords.add(recordKey)) {
            return false;
        }

        csvWriter.writeNext(new String[]{customerId, customerName, recordType, fqdn, name, domainName, data, ttl, tags});
        return true;
    }

    private int checkMaxRows(int totalRecords, int maxRows, int fileCount, CSVWriter csvWriter,
                             BufferedWriter fileWriter, String exportPath) throws IOException {
        if (totalRecords >= maxRows) {
            System.out.println("MaxRows-Grenze erreicht: " + totalRecords + " Einträge. Erstelle neue Datei.");
            csvWriter.close();
            fileWriter.close();

            fileCount++;
            String currentFileName = getNextExportFilename(fileCount);
            fileWriter = new BufferedWriter(new FileWriter(exportPath + currentFileName, false));
            csvWriter = new CSVWriter(fileWriter);

            csvWriter.writeNext(new String[]{
                    "Kunde ID", "Kunde Name", "Record Type", "FQDN", "Name", "Domain Name", "Data", "TTL", "Tags"
            });

            return 0;
        }
        return totalRecords;
    }

    private String getNextExportFilename(int fileCount) {
        return exportFilenameBase.replace(".csv", "_" + fileCount + ".csv");
    }

    private void safeSleep(long millis) {
        try {
           // System.out.println("⏳ Wartezeit von " + millis + "ms, um API-Limit zu vermeiden...");
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("⚠ Warten wurde unterbrochen!");
        }
    }
}
