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
    @GetMapping("api/test/{resourceId}")
    public String getString(@PathVariable String resourceId){
        return "Here is a string for you :0: " + resourceId;
    }

    @PostMapping("api/set-profile")
    public String setProfile(@RequestBody Profile profile) {
        System.out.println("Here is a string for you :0: " + profile);
        return "Profile received: Name - " + profile.getName() + ", Age - " + profile.getAge();
    }

    @PostMapping("api/simple/json")
    public String setProfile(@RequestBody String body) {
        System.out.println("Raw JSON body: " + body);
        return "Received body: " + body;
    }

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


    @PostMapping("api/score/{LatAndLong}")
    public Map<String, Double> scoreByLatAndLong(@PathVariable String LatAndLong) {
        Map<String, Double> response = new HashMap<>();
        return response;
    }
}

class Profile {
    private String name;
    private String age;

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name= name;
    }

    public void setAge(String age) {
        this.age= age;
    }

    public String getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Profile{name='" + name + "', age='" + age + "'}";
    }
}
