package com.test.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.demo.UserAlreadyExistsException;
import com.test.demo.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Main service interface.
 */
@Service
public interface MainService {
    /**
     * Add user to screened users.
     *
     * @param username name of the new user.
     * @throws UserAlreadyExistsException when username is already added.
     */
    void addUser(String username) throws UserAlreadyExistsException;

    /**
     * Remove user from screened users.
     *
     * @param username username of the user to be removed.
     */
    void removeUser(String username);

    /**
     * Return all users from screened users.
     *
     * @return {@code users}.
     */
    Set<UserDTO> getUsers();

    /**
     * Get gists of the user since last visit from github and return raw response.
     *
     * @param username Github username.
     * @return raw string containing user gists.
     * @throws JsonProcessingException if user last visit date is malformed.
     */
    String getRawUserGists(String username) throws JsonProcessingException;

}
