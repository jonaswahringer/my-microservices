package com.jonny.authservice.repositories;

import com.jonny.authservice.entities.ShopUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<ShopUser, Long> {

    Optional<ShopUser> findByLogin(String login);
}
