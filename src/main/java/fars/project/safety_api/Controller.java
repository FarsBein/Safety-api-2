package fars.project.safety_api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {
    @GetMapping("api/health")
    public Map<String, Boolean> health() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    @GetMapping("api/raw/assault-data")
    public JsonNode getRawData() {
        Path jsonDataFilePath = Paths.get("data/assaults.json");
        JsonNode jsonData = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonData = objectMapper.readTree(jsonDataFilePath.toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return jsonData;
    }


    @GetMapping("api/score")
    public Map<String, Object> scoreBasedOnLatAndLong(@RequestParam double lat, @RequestParam double lng) {
        final int ReportsRange = 500;

        Map<String, Object> response = new HashMap<>();
        Map<String, Double> location = new HashMap<>();
        location.put("lat", lat);
        location.put("lng", lng);

        response.put("radius_meters", ReportsRange);
        response.put("time_range_years", 2);
        response.put("location", location);

        JsonNode assaultData = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            assaultData = objectMapper.readTree(Paths.get("data/assaults.json").toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Object> ReportsNearCurrentCoordinates = new ArrayList<>();
        for (JsonNode report : assaultData) {
            double reportLat = report.get("LAT_WGS84").asDouble();
            double reportLong = report.get("LONG_WGS84").asDouble();

            if (distanceInMeters(reportLat, reportLong, lat, lng) > ReportsRange) continue;

            ReportsNearCurrentCoordinates.add(report);
        }
        response.put("report_count", ReportsNearCurrentCoordinates.size());
        response.put("safety_level", safetyLevel(ReportsNearCurrentCoordinates, ReportsRange));
        response.put("reports", ReportsNearCurrentCoordinates);
        return response;
    }

    private double distanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        // (FALSE) using the distance formula, to find the distance between to points on x and y coordinate plane
        // using Haversine formula, because earth is sphere (not flat) thous degrees aren't linear and most be adjusted in order to be converted it into meters https://en.wikipedia.org/wiki/Haversine_formula
        final double R = 6371000;
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * R;
    }

    private String safetyLevel(List<Object> reports, int rangeMeters) {
        //TODO: That take into account the recency of the reports
        //TODO: That take into account the distance of the reports for now we have a fixed range of 500 meters
        int count = reports.size();
        if (count == 0) return "Very Safe";
        else if (count <= 3) return "Safe";
        else if (count <= 7) return "Moderate";
        else if (count <= 15) return "Unsafe";
        else return "Very Unsafe";
    }
}
