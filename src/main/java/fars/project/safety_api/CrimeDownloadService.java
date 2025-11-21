package fars.project.safety_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CrimeDownloadService {
    private final RestTemplate rest =  new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();


    private static final String HUB_DOWNLOAD_API = "https://hub.arcgis.com/api/download/v1/items/b4d0398d37eb4aa184065ed625ddb922/csv?redirect=false&layers=0";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledFetch() {
        try {
            fetchAndProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchAndProcess() throws Exception {
        JsonNode job = requestDownloadJob();
        String status = job.get("status").asText();
        String resultUrl = job.get("resultUrl").asText(null);

        int tries=0;
        while (!status.contains("Completed")) {
            Thread.sleep(5000);
            job = requestDownloadJob();
            status = job.get("status").asText();
            resultUrl = job.get("resultUrl").asText(null);
            tries++;
        }

        if (resultUrl == null) {
            throw new Exception("Could not fetch result URL.\nStatus: " + status +"\nTries: " + tries);
        }

        try (InputStream csvStream = downloadStream(resultUrl)) {
                List<Map<String, String>> filtered = parseFilterCsv(csvStream);
                String json = mapper.writeValueAsString(filtered);
                saveJson(json);
        }

    }

    public List<Map<String, String>> parseFilterCsv(InputStream csvStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        // read the csv
        // filter headers to only get a set of columns = ['OCC_YEAR','PREMISES_TYPE','LONG_WGS84', 'LAT_WGS84','x', 'y']
        // loop through the rows filter by OCC_YEAR

        try (CSVReader reader = new CSVReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {

            // Read the first line, which contains the column headers.
            String[] header = reader.readNext();
            if (header == null) return rows; // empty csv

            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < header.length; i++) headerMap.put(header[i], i);

            String[] columnsToKeep = {"OCC_YEAR","PREMISES_TYPE","LONG_WGS84", "LAT_WGS84","x", "y"};
            Integer cutOffYear = LocalDate.now().minusYears(2).getYear();

            String[] line;

            while ((line = reader.readNext()) != null) {

                // filter rows
                Integer dateIdx = headerMap.get("OCC_YEAR");
                if (line[dateIdx] == null || line[dateIdx].length() < 4) continue;
                int year = Integer.parseInt(line[dateIdx]);
                if (year < cutOffYear) continue;


                // passed rows
                Map<String, String> row = new HashMap<>();
                for (String col : columnsToKeep) {
                    row.put(col, line[headerMap.get(col)]);
                }
                rows.add(row);
            }

        } catch (Exception e) {
            // log the error, and continue processing the rest of the file.
            e.printStackTrace();
        }

        return rows;
    }

    public JsonNode requestDownloadJob() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "SafetyScoreBot/1.0 (+you@example.com)");
        HttpEntity<String> req = new HttpEntity<>(headers);

        ResponseEntity<String> resp = rest.exchange(
                HUB_DOWNLOAD_API,
                HttpMethod.GET,
                req,
                String.class // Expect the response body as a String (which is JSON).
        );

        // convert json string into structured JsonNode object
        return mapper.readTree(resp.getBody());
    }

    private InputStream downloadStream(String url) {
        ResponseEntity<byte[]> resp = rest.getForEntity(URI.create(url), byte[].class);

        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to download CSV: "+resp.getStatusCode());
        }

        return new ByteArrayInputStream(Objects.requireNonNull(resp.getBody()));
    }

    public void saveJson(String json) throws IOException {
        Path path = Paths.get("data/assaults_last_2_years.json");
        Files.createDirectories(path.getParent()); // create folder if doesnt exists
        Files.write(path, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
