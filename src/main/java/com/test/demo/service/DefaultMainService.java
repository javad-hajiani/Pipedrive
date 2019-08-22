package com.test.demo.service;

import com.test.demo.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class DefaultMainService implements MainService {

    @Autowired
    private RestTemplate restTemplate;

    private List<UserDTO> users = new ArrayList<>();

    @Override
    public void addUser(String username) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        users.add(userDTO);
    }

    @Override
    public void removeUser(String username) {
        users.removeIf(userDTO -> userDTO.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public List<UserDTO> getUsers() {
        return users;
    }


    //    @Scheduled(fixedRate = 10 * 1000)
    @Override
    public void callExternalServices() {
        System.out.println("called");
        users.forEach(userDTO -> getGitHubGists(userDTO.getUsername()));
    }

    public String getGitHubGists(String username) {
        String url = String.format("https://api.github.com/users/%s/gists", username);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        return responseEntity.getBody();
    }
}
