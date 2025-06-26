package com.example.paisafy.Model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String profilePic;
    private String joinDate;

    public User(int id, String name, String email, String password, String profilePic, String joinDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.profilePic = profilePic;
        this.joinDate = joinDate;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getProfilePic() { return profilePic; }
    public String getJoinDate() { return joinDate; }
}