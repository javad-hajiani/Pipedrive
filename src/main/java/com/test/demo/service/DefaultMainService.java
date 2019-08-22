package com.test.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.test.demo.UserAlreadyExistsException;
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
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
     * A Set containing all user being screened.
     */
    private Set<UserDTO> users = new HashSet<>();

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
     * Handler to run right after construction of this class.
     * Used to load users data.
     */
    @PostConstruct
    private void postConstruct() {
        try {
            loadUsersFromFile();
            logger.info("Loaded user data from file.");
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error loading users from file ", e);
        }
    }

    /**
     * Handler to run just before destruction of this class.
     * Used to save users data.
     */
    @PreDestroy
    private void preDestroy() {
        try {
            saveUsersAsFile();
            logger.info("Saved user data to file.");
        } catch (IOException e) {
            logger.error("Error saving users to file ", e);
        }
    }

    /**
     * Add user to {@link #users} if it does not exist.
     *
     * @param username name of the new user.
     * @throws UserAlreadyExistsException when username is already added.
     */
    @Override
    public void addUser(String username) throws UserAlreadyExistsException {
        if (users.stream().noneMatch(userDTO -> userDTO.getUsername().equalsIgnoreCase(username))) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            users.add(userDTO);
        }else {
            throw new UserAlreadyExistsException("Username already exists");
        }
    }

    /**
     * Remove user from {@link #users}.
     *
     * @param username username of the user to be removed.
     */
    @Override
    public void removeUser(String username) {
        users.removeIf(userDTO -> userDTO.getUsername().equalsIgnoreCase(username));
    }

    /**
     * Return all users from {@link #users}.
     * Note that this returns a copy of users, not a reference.
     *
     * @return {@code users}.
     */
    @Override
    public Set<UserDTO> getUsers() {
        return new HashSet<>(users);
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
        users.remove(userDTO);
        userDTO.setLastVisit(new Date());
        users.add(userDTO);
        return responseEntity.getBody();
    }

    /**
     * For all users added to {@link #users} list, get their new gists and
     * for each gist create a pipedrive activity.
     *
     * @throws JsonProcessingException if {@link UserDTO#lastAdded} is malformed.
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // run every hour, whether last run is finished or not
    private void processGists() throws JsonProcessingException {
        logger.info("Started processing users...");
        for (UserDTO userDTO : users) {
            logger.info("/**********************************");
            String since = "";
            if (userDTO.getLastAdded() != null) {
                since = mapper.writeValueAsString(userDTO.getLastAdded());
            }
            List<GistDto> gistDtos = getGitHubGists(userDTO.getUsername(), since);
            logger.info("User: "+userDTO);
            logger.info("Gists count since last visit: {}",gistDtos.size());
            for (GistDto gistDto : gistDtos) {
                if (!addPipeDriveActivity(gistDto.getId(), gistDto.getUrl())) {
                    logger.error(String.format("Error adding activity for gist id:%s", gistDto.getId()));
                }else {
                    logger.info("Pipe Drive activity added.");
                }
            }
            userDTO.setLastAdded(new Date());
            logger.info("**********************************/");

        }
        logger.info("Ended processing users...");
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

    /**
     * A simple way to persist current user data to a local file.
     *
     * @throws IOException if file does not exist.
     */
    private void saveUsersAsFile() throws IOException {
        FileOutputStream fos = new FileOutputStream("users.data");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(users);
        oos.close();
    }

    /**
     * Load users data from file.
     *
     * @throws IOException            if file is not found or corrupt.
     * @throws ClassNotFoundException if {@link UserDTO} is not found/loaded.
     */
    private void loadUsersFromFile() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("users.data");
        ObjectInputStream ois = new ObjectInputStream(fis);
        users = (Set<UserDTO>) ois.readObject();
        ois.close();
    }
}
