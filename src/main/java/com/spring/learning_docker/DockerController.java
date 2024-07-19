package com.spring.learning_docker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DockerController {

    @GetMapping("/")
    public String helloDocker() {
        return """
                { message: Hello Docker from Container! }
               """;
    }

}
