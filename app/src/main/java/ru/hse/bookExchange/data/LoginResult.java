package ru.hse.bookExchange.data;

import androidx.annotation.Nullable;

import ru.hse.bookExchange.data.model.LoggedInUser;

/**
 * Authentication result : success (user details) or error message.
 */
public class LoginResult {
    @Nullable
    private LoggedInUser success;
    @Nullable
    private Integer error;

    public LoginResult(@Nullable Integer error) {
        this.error = error;
    }

    public LoginResult(@Nullable LoggedInUser success) {
        this.success = success;
    }

    @Nullable
    public LoggedInUser getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
}