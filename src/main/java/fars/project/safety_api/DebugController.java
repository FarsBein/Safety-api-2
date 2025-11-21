package fars.project.safety_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final CrimeDownloadService fetcher;

    public DebugController(CrimeDownloadService fetcher) {
        this.fetcher = fetcher;
    }

    @GetMapping("/csv")
    public String testCsv() {
        try {
            fetcher.fetchAndProcess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "success";
    }

}
