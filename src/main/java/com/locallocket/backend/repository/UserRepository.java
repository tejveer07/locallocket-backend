package com.locallocket.backend.repository;

import com.locallocket.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.email = :emailOrPhone OR u.phoneNumber = :emailOrPhone")
    Optional<User> findByEmailOrPhoneNumber(@Param("emailOrPhone") String emailOrPhone);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email OR u.phoneNumber = :phoneNumber")
    boolean existsByEmailOrPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber);
}

