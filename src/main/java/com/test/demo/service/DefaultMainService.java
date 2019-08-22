package com.test.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.test.demo.dto.GistDto;
import com.test.demo.dto.PipeDriveResponse;
import com.test.demo.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Primary implementation of the required methods.
 */
@Service
public class DefaultMainService implements MainService {

    /**
     * A logger.
     */
    private Logger logger = LoggerFactory.getLogger(DefaultMainService.class);

    /**
     * PipeDrive Token is held in this field.
     * There will be an exception on the startup if required environment variable is not found.
     */
    @Value("${PIPEDRIVE_TOKEN}")
    private String pipeDriveToken;

    /**
     * Injected {@link RestTemplate}.
     */
    private final RestTemplate restTemplate;

    /**
     * Jackson Object mapper, used for date to ISO 8601 conversion.
     */
    private ObjectMapper mapper = new ObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    /**
     * A list containing all user being screened.
     */
    private List<UserDTO> users = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param restTemplate Rest Template.
     */
    @Autowired
    public DefaultMainService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Add user to {@link #users} list.
     *
     * @param username name of the new user.
     */
    @Override
    public void addUser(String username) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        users.add(userDTO);
    }

    /**
     * Remove user from {@link #users} list.
     *
     * @param username username of the user to be removed.
     */
    @Override
    public void removeUser(String username) {
        users.removeIf(userDTO -> userDTO.getUsername().equalsIgnoreCase(username));
    }

    /**
     * Return all users from {@link #users} list.
     *
     * @return {@code users}.
     */
    @Override
    public List<UserDTO> getUsers() {
        return users;
    }

    /**
     * Get gists of the user since last visit from github and return raw response.
     *
     * @param username Github username.
     * @return raw string containing user gists.
     * @throws JsonProcessingException if user last visit date is malformed.
     */
    @Override
    public String getRawUserGists(String username) throws JsonProcessingException {

        Optional<UserDTO> first = users.stream().filter(userDTO -> userDTO.getUsername().equalsIgnoreCase(username)).findFirst();
        if (!first.isPresent()) return "";
        UserDTO userDTO = first.get();
        String since = "";
        if (userDTO.getLastVisit() != null) {
            since = mapper.writeValueAsString(first.get().getLastVisit());
        }
        String url = getGitHubUrl(username, since);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        users.get(users.indexOf(userDTO)).setLastVisit(new Date());
        return responseEntity.getBody();
    }

    /**
     * For all users added to {@link #users} list, get their new gists and
     * for each gist create a pipedrive activity.
     *
     * @throws JsonProcessingException if {@link UserDTO#lastAdded} is malformed.
     */
    @Scheduled(fixedRate = 4 * 60 * 60 * 1000) // every four hours, whether last run is finished or not
    private void callExternalServices() throws JsonProcessingException {
        for (UserDTO userDTO : users) {
            String since = "";
            if (userDTO.getLastAdded() != null) {
                since = mapper.writeValueAsString(userDTO.getLastAdded());
            }
            List<GistDto> gistDtos = getGitHubGists(userDTO.getUsername(), since);

            for (GistDto gistDto : gistDtos) {
                if (!addPipeDriveActivity(gistDto.getId(), gistDto.getUrl())) {
                    logger.error(String.format("Error adding activity for gist id:%s", gistDto.getId()));
                }
            }
            userDTO.setLastAdded(new Date());
        }
    }

    /**
     * Get new gists from github.
     *
     * @param username Github username.
     * @param since    Defines the timestamp since which the data should be queried.
     * @return a list of {@link GistDto}
     */
    private List<GistDto> getGitHubGists(String username, String since) {
        String url = getGitHubUrl(username, since);
        ResponseEntity<GistDto[]> responseEntity = restTemplate.getForEntity(url, GistDto[].class);
        return Arrays.asList(responseEntity.getBody());
    }

    /**
     * Add an activity to Pipe Drive, containing the id and url of a gist.
     *
     * @param gistId  Id of the gist.
     * @param gistUrl Url of the gist.
     * @return true if the activity is added successfully.
     */
    private boolean addPipeDriveActivity(String gistId, String gistUrl) {
        String url = String.format("https://api.pipedrive.com/v1/activities?api_token=%s", pipeDriveToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        Map<String, String> body = new HashMap<>();
        body.put("subject", String.format("Gitst #%s", gistId));
        body.put("done", "0");
        body.put("type", "github-gist");
        body.put("note", gistUrl);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        ResponseEntity<PipeDriveResponse> responseEntity = restTemplate.postForEntity(url, entity, PipeDriveResponse.class);

        return responseEntity.getBody().getSuccess();
    }

    /**
     * Prepare a url to get data from github.
     *
     * @param username Github username.
     * @param since    Defines the timestamp since which the data should be queried.
     * @return prepared url.
     */
    private String getGitHubUrl(String username, String since) {
        String url = String.format("https://api.github.com/users/%s/gists", username);
        if (since != null) {
            url = url.concat("?since=").concat(since);
        }
        return url;
    }
}
