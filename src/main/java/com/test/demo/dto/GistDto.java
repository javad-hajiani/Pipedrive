package com.test.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO to hold gist information.
 */
public class GistDto {
    /**
     * Id of the gist.
     */
    private String id;

    /**
     * URL to html view of the gist.
     */
    @JsonProperty("html_url")
    private String url;

    /**
     * @return {@code id}
     * @see #id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id {@code id}.
     * @see #id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return {@code url}.
     * @see #url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url {@code url}.
     * @see #url
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
