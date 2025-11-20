package fars.project.safety_api;

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
        System.out.println("Raw JSON body>>> " + body);
        return "Received body: " + body;
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
