package com.gonaturefarms.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String role;

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    // Static builder method as failsafe for Lombok @Builder
    public static UserSummaryBuilder builder() {
        return new UserSummaryBuilder();
    }

    public static class UserSummaryBuilder {
        private Long id;
        private String name;
        private String phone;
        private String email;
        private String role;

        public UserSummaryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserSummaryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserSummaryBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserSummaryBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserSummaryBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserSummary build() {
            UserSummary summary = new UserSummary();
            summary.id = this.id;
            summary.name = this.name;
            summary.phone = this.phone;
            summary.email = this.email;
            summary.role = this.role;
            return summary;
        }
    }
}
