package ru.hse.bookExchange.data.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.List;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser implements Serializable {
    private Long id;
    private String username;
    private String name;
    private transient Bitmap avatar;
    private Long avatarId;
    private String token;
    private List<Long> wishListIds;
    private String role;

    public LoggedInUser() {
    }

    public LoggedInUser(Long id, String token, String username, String name,
                        List<Long> wishListIds, Long avatarId, String role) {
        this.id = id;
        this.token = token;
        this.username = username;
        this.name = name;
        this.wishListIds = wishListIds;
        this.wishListIds = wishListIds;
        this.avatarId = avatarId;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getWishListIds() {
        return wishListIds;
    }

    public void setWishListIds(List<Long> wishListIds) {
        this.wishListIds = wishListIds;
    }

    public Long getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

}