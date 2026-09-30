package com.gestaofinanceira.modules.auth;

import com.gestaofinanceira.modules.user.Role;
import com.gestaofinanceira.modules.user.User;

import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private Role role;

    public UserDTO() {}

    public UserDTO(UUID id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static UserDTO fromEntity(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
