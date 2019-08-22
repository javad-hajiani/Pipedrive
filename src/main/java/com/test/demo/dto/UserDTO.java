package com.test.demo.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO to hold values user information.
 */
public class UserDTO implements Serializable {

    /**
     * Username of the user.
     */
    private String username;

    /**
     * Last timestamp that the gists of this user has been retrieved.
     */
    private Date lastVisit;

    /**
     * Last timestamp that the gists of this user has been added as activity.
     */
    private Date lastAdded;

    /**
     * @see #lastAdded
     * @return {@code lastAdded}.
     */
    public Date getLastAdded() {
        return lastAdded;
    }

    /**
     * @see #lastAdded
     * @param lastAdded {@code lastAdded}.
     */
    public void setLastAdded(Date lastAdded) {
        this.lastAdded = lastAdded;
    }

    public String getUsername() {
        return username;
    }

    /**
     * @see #username
     * @param username {@code username}.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @see #lastVisit
     * @return {@code lastVisit}
     */
    public Date getLastVisit() {
        return lastVisit;
    }

    /**
     * @see #lastVisit
     * @param lastVisit {@code lastVisit}
     */
    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", lastVisit=" + lastVisit +
                ", lastAdded=" + lastAdded +
                '}';
    }
}
