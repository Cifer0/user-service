package com.demo.user.service;

import com.demo.user.controller.UserController;
import com.demo.user.dto.UserDTO;
import com.demo.user.entity.NameEntity;
import com.demo.user.entity.UserEntity;
import com.demo.user.repository.NameRepository;
import com.demo.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Domain Layer of the user-service.
 * Responsible for applying domain logic on requested {@link UserEntity} for {@link UserRepository} and response {@link UserDTO} for {@link UserController}.
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    private final NameRepository nameRepository;

    public UserService(UserRepository userRepository, NameRepository nameRepository) {
        this.userRepository = userRepository;
        this.nameRepository = nameRepository;
    }

    /**
     * Called by {@link UserController#getUser(String, String)} and calls {@link UserRepository#findByUsername(String)}.
     * Performs a Dark Read in order to ensure data integrity.
     * @param username identifying username
     * @param version specified resource representation version
     * @return Optional of {@link UserDTO} of found {@link UserEntity} if successful or of an error message if data integrity is breached
     *      empty Optional if {@link UserEntity} not found
     */
    public Optional<UserDTO> findUser(final String username, final String version) {
        final Optional<UserEntity> optUserEntity = this.userRepository.findByUsername(username);
        if (optUserEntity.isEmpty()) return Optional.empty();

        UserEntity userEntity = optUserEntity.get();
        if (!this.dataIntegrityIsProvided(userEntity)) {
            UserDTO errorDTO = new UserDTO("error", "500", "inconsistent data");
            return Optional.of(errorDTO);
        }

        return Optional.of(getUserDtoFromUserEntityByVersion(userEntity, version));
    }

    /**
     * Creates new {@link UserEntity} by given username with the content of the appended {@link UserDTO} by specified version.
     * The corresponding attributes of the appended {@link UserDTO} by which to create the {@link UserEntity} can be assumed to be null-safe by version
     * as the request body is checked for validity in {@link UserController}.
     * @param username identifying username
     * @param version specified resource representation version
     * @param userDTO appended user data
     * @return Optional of {@link UserDTO} of created {@link UserEntity} if successful or of an error message if data integrity is breached
     *      empty Optional if not
     */
    public Optional<UserDTO> createUser(final String username, final String version, final UserDTO userDTO) {
        if (this.userRepository.findByUsername(username).isPresent()) return Optional.empty();

        final UserEntity userEntity;
        userEntity = new UserEntity(username, userDTO.getFirstName(), userDTO.getLastName());

        if (!dataIntegrityIsProvided(userEntity)) {
            UserDTO errorDTO = new UserDTO("error", "500", "inconsistent data");
            return Optional.of(errorDTO);
        }

        this.userRepository.save(userEntity);

        return Optional.of(getUserDtoFromUserEntityByVersion(userEntity, version));
    }

    /**
     * Updates {@link UserEntity} by given username with the content of the appended {@link UserDTO} by specified version.
     * Called by {@link UserController#putUser(String, String, UserDTO)}.
     * @param username identifying username for updating of corresponding {@link UserEntity}
     * @param version  specified resource representation version
     * @param userDTO  appended user data to update {@link UserEntity} with
     * @return {@link UserDTO} of the updated {@link UserEntity} or of an error message if data integrity is breached
     *      empty Optional if {@link UserEntity} not found
     */
    public Optional<UserDTO> updateUser(final String username, final String version, final UserDTO userDTO) {
        Optional<UserEntity> optUserEntity = this.userRepository.findByUsername(username);

        if (optUserEntity.isEmpty()) return Optional.empty();

        final UserEntity userEntity = optUserEntity.get();
        if (userDTO.getFirstName() != null) userEntity.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) userEntity.setLastName(userDTO.getLastName());

        if (!this.dataIntegrityIsProvided(userEntity)) {
            UserDTO errorDTO = new UserDTO("error", "500", "inconsistent data");
            return Optional.of(errorDTO);
        }

        this.userRepository.save(userEntity);

        return Optional.of(getUserDtoFromUserEntityByVersion(userEntity, version));
    }

    /**
     * Called by {@link UserController#deleteUser(String, String)} and calls {@link UserRepository#deleteByUsername(String)}.
     * @param username identifying username
     * @return Optional of {@link UserDTO} of the deleted {@link UserEntity} if successful or of an error message if data integrity is breached
     *      empty Optional if user not found
     */
    public Optional<UserDTO> deleteUser(final String username, final String version) {
        List<UserEntity> userEntityList = this.userRepository.deleteByUsername(username);

        if (userEntityList.isEmpty()) return Optional.empty();

        UserEntity userEntity = userEntityList.get(0);
        NameEntity nameEntity = userEntity.getNameEntity();
        if (this.nameRepository.findById(nameEntity.getId()).isPresent()) {
            UserDTO errorDTO = new UserDTO("error", "500", "inconsistent data");
            return Optional.of(errorDTO);
        }

        return Optional.of(getUserDtoFromUserEntityByVersion(userEntity, version));
    }

    /**
     * Migrates old {@link UserEntity} by adding a corresponding {@link NameEntity}.
     * Called by {@link UserController#migrateUsers()}.
     * @return List of {@link UserDTO} of migrated {@link UserEntity}
     */
    public List<UserDTO> migrateUsers() {
        List<UserEntity> usersToMigrate = this.userRepository.getByNameEntityIsNull();
        List<UserDTO> migratedUsers = new ArrayList<>();

        for (UserEntity userEntity : usersToMigrate) {
            userEntity.setNameEntity(new NameEntity(userEntity.getFirstName(), userEntity.getLastName()));
            this.userRepository.save(userEntity);
            migratedUsers.add(new UserDTO(userEntity.getUsername(), userEntity.getFirstName(), userEntity.getLastName()));
        }

        return migratedUsers;
    }

    /**
     * Utility function that checks for data integrity between {@link UserEntity} and {@link NameEntity}.
     * @param userEntity {@link UserEntity} to be checked
     * @return true if data integrity is provided
     */
    private boolean dataIntegrityIsProvided(UserEntity userEntity) {
        final NameEntity nameEntity = userEntity.getNameEntity();

        if (nameEntity == null) return true;
        return userEntity.getFirstName().equals(nameEntity.getFirstName()) && userEntity.getLastName().equals(nameEntity.getLastName());
    }

    /**
     * Utility function that returns an abstraction of a given {@link UserEntity} as {@link UserDTO} to {@link UserController} by version for {@link #findUser(String, String)}, {@link #createUser(String, String, UserDTO)} and {@link #updateUser(String, String, UserDTO)}.
     * The version parameter serves as a placeholder for when there are different versions to be supported in parallel.
     * @param userEntity {@link UserEntity} for abstraction
     * @param version specified resource representation version
     * @return {@link UserDTO} as abstraction of {@link UserEntity} for given version
     */
    private UserDTO getUserDtoFromUserEntityByVersion(UserEntity userEntity, String version) {
        return new UserDTO(userEntity.getUsername(), userEntity.getFirstName(), userEntity.getLastName());
    }
}
