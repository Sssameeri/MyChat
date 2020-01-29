package com.android.sssameeri.mychat.models;

public class User {
    private String name;
    private String email;
    private String id;
    private int photoMockUp;

    public User() {
    }

    public User(String name, String email, String id, int photoMockUp) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.photoMockUp = photoMockUp;
    }

    public int getPhotoMockUp() {
        return photoMockUp;
    }

    public void setPhotoMockUp(int photoMockUp) {
        this.photoMockUp = photoMockUp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
