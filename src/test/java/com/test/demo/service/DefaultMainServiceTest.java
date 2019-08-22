package com.test.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.demo.UserAlreadyExistsException;
import com.test.demo.dto.UserDTO;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

/**
 * Tests related to {@link DefaultMainService}.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DefaultMainServiceTest {

    /**
     * An existing github user who has a lot of gists.
     */
    private final String EXISTING_USER_USERNAME = "mattboldt"; // just a random valid username
    /**
     * A user which is not in {@link DefaultMainService#users}.
     */
    private final String NEW_USER_USERNAME = "NewUser";

    /**
     * Injected service.
     */
    @Autowired
    private MainService mainService;

    /**
     * Preparation of data before running tests.
     */
    @Before
    public void before() {
        try {
            cleanUp();
            mainService.addUser(EXISTING_USER_USERNAME);
        } catch (UserAlreadyExistsException ignore) {
        }
    }

    /**
     * Required steps after running tests.
     */
    @After
    public void after() {
        cleanUp();
    }

    /**
     * Cleanup method to removed test data.
     */
    private void cleanUp() {
        mainService.removeUser(NEW_USER_USERNAME);
        mainService.removeUser(EXISTING_USER_USERNAME);
    }

    /**
     * Test to add a new user. No exception is expected.
     *
     * @throws UserAlreadyExistsException if user exists.
     */
    @Test
    public void addUser_NewUser_ShouldSucceed() throws UserAlreadyExistsException {
        Set<UserDTO> users = mainService.getUsers();
        mainService.addUser(NEW_USER_USERNAME);
        Set<UserDTO> newUsers = mainService.getUsers();

        Assertions.assertThat(users.size()).isLessThan(newUsers.size());
        Assertions.assertThat(users.size()).isEqualTo(newUsers.size() - 1); // Only one item must be added
        Assert.assertTrue(newUsers.stream()
                .anyMatch(userDTO -> userDTO.getUsername().equalsIgnoreCase(NEW_USER_USERNAME)));
    }

    /**
     * Add a duplicate user, should throw exception.
     *
     * @throws UserAlreadyExistsException when username is duplicate.
     */
    @Test(expected = UserAlreadyExistsException.class)
    public void addUser_ExistingUser_ShouldThrowException() throws UserAlreadyExistsException {
        mainService.addUser(EXISTING_USER_USERNAME);
    }

    /**
     * Remove a user which is not screened. should succeed.
     */
    @Test
    public void removeUser_UserDoesNotExist_ShouldSucceed() {
        Set<UserDTO> users = mainService.getUsers();
        mainService.removeUser("NonExistingUser");
        Set<UserDTO> newUsers = mainService.getUsers();

        Assertions.assertThat(users.size()).isEqualTo(newUsers.size());
    }

    /**
     * Remove a user which is being screened.
     */
    @Test
    public void removeUser_UserExists_ShouldSucceed() {
        Set<UserDTO> users = mainService.getUsers();
        mainService.removeUser(EXISTING_USER_USERNAME);
        Set<UserDTO> newUsers = mainService.getUsers();

        Assertions.assertThat(users.size()).isGreaterThan(newUsers.size());
        Assertions.assertThat(users.size()).isEqualTo(newUsers.size() + 1); // Only one item must be deleted
        Assert.assertTrue(newUsers.stream()
                .noneMatch(userDTO -> userDTO.getUsername().equalsIgnoreCase(EXISTING_USER_USERNAME)));
    }

    /**
     * Get gists of a user as a raw string.
     *
     * @throws JsonProcessingException if user related dates are not valid.
     */
    @Test
    public void getRawUserGists_GistsExistSinceLastVisit_ShouldReturnGists() throws JsonProcessingException {
        String userGists = mainService.getRawUserGists(EXISTING_USER_USERNAME);
        Assertions.assertThat(userGists).isNotEmpty();
        Assertions.assertThat(userGists).isNotEqualTo("[]");
    }

    /**
     * Get gists for a user. Since there are no new gists between two visits,
     * an empty result is expected.
     *
     * @throws JsonProcessingException if dates are corrupted.
     */
    @Test
    public void getRawUserGists_GistsNotExistSinceLastVisit_ShouldReturnEmpty() throws JsonProcessingException {
        String userGists = mainService.getRawUserGists(EXISTING_USER_USERNAME);
        Assertions.assertThat(userGists).isNotEmpty();
        Assertions.assertThat(userGists).isNotEqualTo("[]");

        userGists = mainService.getRawUserGists(EXISTING_USER_USERNAME); // Since last visit
        Assertions.assertThat(userGists).isEqualTo("[]");

    }

    /**
     * Get gists for a user not being screened. Empty result is expected.
     *
     * @throws JsonProcessingException If dates are corrupted.
     */
    @Test
    public void getRawUserGists_InvalidUserName_ShouldReturnGists() throws JsonProcessingException {
        String userGists = mainService.getRawUserGists(NEW_USER_USERNAME);
        Assertions.assertThat(userGists).isEmpty();
    }
}
