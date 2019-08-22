package com.test.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.demo.dto.UserDTO;
import com.test.demo.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to interact with users.
 */
@RestController
public class MainController {

    /**
     * Injected Main Service.
     */
    private final MainService mainService;

    /**
     * Constructor.
     *
     * @param mainService Main Service.
     */
    @Autowired
    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    /**
     * Endpoint to add users for screening.
     *
     * @param username Username of the new user.
     * @return Http 200 if successful.
     */
    @PostMapping("/add-user")
    public ResponseEntity addUser(@RequestParam String username) {
        mainService.addUser(username);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to delete users from screening.
     *
     * @param username Username of the new user.
     * @return Http 200 if successful.
     */
    @DeleteMapping("/delete-user")
    public ResponseEntity deleteUser(@RequestParam String username) {
        mainService.removeUser(username);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to get all screened users.
     *
     * @return a json list of {@link UserDTO}.
     */
    @GetMapping("/users")
    public ResponseEntity getUsers() {
        return ResponseEntity.ok(mainService.getUsers());
    }

    /**
     * Endpoint to get all gists of a user, since last visit.
     *
     * @param username username of the user.
     * @return a all gists since last visit.
     * @throws JsonProcessingException if {@link UserDTO#lastVisit} is malformed.
     */
    @GetMapping("/gists/{username}")
    public ResponseEntity getUserGists(@PathVariable(name = "username") String username) throws JsonProcessingException {
        return ResponseEntity.ok(mainService.getRawUserGists(username));
    }
}
