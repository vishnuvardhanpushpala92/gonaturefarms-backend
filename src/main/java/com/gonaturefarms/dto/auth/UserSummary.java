package com.gonaturefarms.dto.auth;

import com.gonaturefarms.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSummary {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String role;

    // Static factory method to convert User entity to UserSummary
    public static UserSummary from(User user) {
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setName(user.getName());
        summary.setPhone(user.getPhone());
        summary.setEmail(user.getEmail());
        summary.setRole(user.getRole() != null ? user.getRole().name() : null);
        return summary;
    }

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
}