package com.tranmaunhan.example05.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tranmaunhan.example05.entities.User;



@Repository
public interface UserRepo extends JpaRepository<User, Long> {

@Query("SELECT u FROM User u JOIN FETCH u.addresses a WHERE a.addressId = ?1")
    List<User> findByAddress(Long addressId);



    @Query("""
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN FETCH u.roles
        WHERE u.email = :email
    """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    Optional<User> findByEmail(String email);


}