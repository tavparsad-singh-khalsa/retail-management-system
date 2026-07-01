package com.retail.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sales")
public class SalesController {

    @GetMapping
    public ResponseEntity<String> getSales() {
        return ResponseEntity.ok("Sales controller is up and running!");
    }
}
