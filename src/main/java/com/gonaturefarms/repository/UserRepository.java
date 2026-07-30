package com.gonaturefarms.repository;

import com.gonaturefarms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByPhoneOrEmail(String phone, String email);

    boolean existsByPhone(String phone);

    /** Mirrors: SELECT * FROM users WHERE role='admin' AND (phone=? OR name=? OR email=?) LIMIT 1 */
    @Query("SELECT u FROM User u WHERE u.role = :role " +
           "AND (u.phone = :identifier OR u.name = :identifier OR u.email = :identifier)")
    Optional<User> findFirstByRoleAndIdentifier(@Param("role") User.UserRole role,
                                                 @Param("identifier") String identifier);

    long countByRole(User.UserRole role);
}
