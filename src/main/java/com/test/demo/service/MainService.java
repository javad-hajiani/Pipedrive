package com.test.demo.service;

import com.test.demo.dto.UserDTO;

import java.util.List;

/**
 *
 */
public interface MainService {
    void addUser(String username);

    void removeUser(String username);

    List<UserDTO> getUsers();

    void callExternalServices();
}
