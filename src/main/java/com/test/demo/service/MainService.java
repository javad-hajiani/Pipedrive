package com.test.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.demo.dto.UserDTO;

import java.util.List;

/**
 * Main service interface.
 */
public interface MainService {
    /**
     * Add user to screened users.
     * @param username name of the new user.
     */
    void addUser(String username);

    /**
     * Remove user from screened users.
     * @param username username of the user to be removed.
     */
    void removeUser(String username);

    /**
     * Return all users from screened users.
     * @return {@code users}.
     */
    List<UserDTO> getUsers();

    /**
     * Get gists of the user since last visit from github and return raw response.
     * @param username Github username.
     * @return raw string containing user gists.
     * @throws JsonProcessingException if user last visit date is malformed.
     */
    String getRawUserGists(String username) throws JsonProcessingException;

}
