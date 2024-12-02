package com.safequery.back.projectdemo.Resposirty;

import com.safequery.back.projectdemo.Model.UserDAO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDAO, Long> {
   UserDAO findByUsername(String username);
    Optional<UserDAO> findByEmail(String email);

}
