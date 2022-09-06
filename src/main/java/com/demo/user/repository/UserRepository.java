package com.demo.user.repository;

import com.demo.user.entity.UserEntity;
import com.demo.user.service.UserService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data layer of the user-service.
 * Responsible for querying the database and providing {@link UserService} with {@link UserEntity}.
 */
@Repository
@Transactional
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds {@link UserEntity} by given username.
     * @param username identifying username
     * @return found {@link UserEntity}
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Deletes and returns {@link UserEntity} by given username.
     * @param username identifying username
     * @return deleted {@link UserEntity}
     */
    List<UserEntity> deleteByUsername(String username);
}
