package com.test.demo.dto;

/**
 * Representation of response received from pipedrive.
 */
public class PipeDriveResponse {
    /**
     * Whether api call was successful.
     */
    private Boolean success;

    /**
     * @return {@code success}
     * @see #success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * @param success {@code success}.
     * @see #success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
