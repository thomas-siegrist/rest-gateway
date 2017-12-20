package ch.sbb.api.restgateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PingController {

    @RequestMapping(
            value = "/ping/**",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Pong");
    }

}
