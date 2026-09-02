package com.gonaturefarms.repository;

import com.gonaturefarms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByPhoneOrEmail(String phone, String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    List<User> findByRole(User.UserRole role);

    /** Allow login by username, phone, or email for customers */
    @Query("SELECT u FROM User u WHERE u.role = 'customer' " +
           "AND (u.phone = :identifier OR u.name = :identifier OR u.email = :identifier)")
    Optional<User> findCustomerByIdentifier(@Param("identifier") String identifier);

    /** Mirrors: SELECT * FROM users WHERE role='admin' AND (phone=? OR name=? OR email=?) LIMIT 1 */
    @Query("SELECT u FROM User u WHERE u.role = :role " +
           "AND (u.phone = :identifier OR u.name = :identifier OR u.email = :identifier)")
    Optional<User> findFirstByRoleAndIdentifier(@Param("role") User.UserRole role,
                                                 @Param("identifier") String identifier);

    @Query("SELECT u FROM User u WHERE u.role = 'customer' AND (u.whatsappOptOut IS NULL OR u.whatsappOptOut = false)")
    List<User> findCustomersWhoHaveNotOptedOut();

    long countByRole(User.UserRole role);
}
