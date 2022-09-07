package com.demo.user.repository;

import com.demo.user.entity.NameEntity;
import com.demo.user.service.UserService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

/**
 * Data layer of the user-service.
 * Responsible for querying the database and providing {@link UserService} with {@link NameEntity}.
 */
@Repository
@Transactional
public interface NameRepository extends JpaRepository<NameEntity, UUID> {
}
