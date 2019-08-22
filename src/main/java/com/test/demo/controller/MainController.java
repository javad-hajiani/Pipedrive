package com.test.demo.controller;

import com.test.demo.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author amir
 */
@RestController
public class MainController {

    private final MainService mainService;

    @Autowired
    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @PostMapping("/add-user")
    public ResponseEntity addUser(@RequestParam String username) {
        mainService.addUser(username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity deleteUser(@RequestParam String username) {
        mainService.removeUser(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity getUsers() {
        return ResponseEntity.ok(mainService.getUsers());
    }


}
