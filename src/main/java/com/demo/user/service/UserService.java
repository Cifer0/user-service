package com.demo.user.service;

import com.demo.user.controller.UserController;
import com.demo.user.dto.UserDTO;
import com.demo.user.entity.UserEntity;
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

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Called by {@link UserController#getUser(String, String)} and calls {@link UserRepository#findByUsername(String)}.
     * @param username identifying username
     * @param version specified resource representation version
     * @return Optional of {@link UserDTO} of found {@link UserEntity} if successful or an empty Optional if not
     */
    public Optional<UserDTO> findUser(final String username, final String version) {
        final Optional<UserEntity> optUserEntity = this.userRepository.findByUsername(username);

        if (optUserEntity.isEmpty()) return Optional.empty();

        UserEntity userEntity = optUserEntity.get();
        return Optional.of(getUserDtoFromUserEntityByVersion(userEntity, version));
    }

    /**
     * Creates new {@link UserEntity} by given username with the content of the appended {@link UserDTO} by specified version.
     * The corresponding attributes of the appended {@link UserDTO} by which to create the {@link UserEntity} can be assumed to be null-safe by version
     * as the request body is checked for validity in {@link UserController}.
     * @param username identifying username
     * @param version specified resource representation version
     * @param userDTO appended user data
     * @return Optional of {@link UserDTO} of created {@link UserEntity} if successful or an empty Optional if not
     */
    public Optional<UserDTO> createUser(final String username, final String version, final UserDTO userDTO) {
        if (this.userRepository.findByUsername(username).isPresent()) return Optional.empty();

        final UserEntity userEntity;
        if (version.equals("1")) {
            userEntity = new UserEntity(username, userDTO.getFullName());
        } else {
            userEntity = new UserEntity(username, userDTO.getFirstName(), userDTO.getLastName());
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
     * @return {@link UserDTO} of the updated {@link UserEntity}
     */
    public Optional<UserDTO> updateUser(final String username, final String version, final UserDTO userDTO) {
        Optional<UserEntity> optUserEntity = this.userRepository.findByUsername(username);

        if (optUserEntity.isEmpty()) return Optional.empty();

        final UserEntity userEntity = optUserEntity.get();
        if (version != null) {
            if (version.equals("1")) {
                if (userDTO.getFullName() != null) userEntity.setFullName(userDTO.getFullName());
            } else if (version.equals("2")) {
                if (userDTO.getFirstName() != null) userEntity.setFirstName(userDTO.getFirstName());
                if (userDTO.getLastName() != null) userEntity.setLastName(userDTO.getLastName());
            }
        } else {
            if (userDTO.getFullName() != null) userEntity.setFullName(userDTO.getFullName());
            if (userDTO.getFirstName() != null) userEntity.setFirstName(userDTO.getFirstName());
            if (userDTO.getLastName() != null) userEntity.setLastName(userDTO.getLastName());
        }

        this.userRepository.save(userEntity);

        return Optional.of(getUserDtoFromUserEntityByVersion(userEntity, version));
    }

    /**
     * Called by {@link UserController#deleteUser(String, String)} and calls {@link UserRepository#deleteByUsername(String)}.
     * @param username identifying username
     * @return Optional of {@link UserDTO} of the deleted {@link UserEntity} if successful or an empty Optional if not.
     */
    public Optional<UserDTO> deleteUser(final String username, final String version) {
        List<UserEntity> userEntityList = this.userRepository.deleteByUsername(username);

        if (userEntityList.isEmpty()) return Optional.empty();

        UserEntity userEntity = userEntityList.get(0);
        return Optional.of(getUserDtoFromUserEntityByVersion(userEntity, version));
    }

    /**
     * Migrates old {@link UserEntity} by deleting the old entry and saving it again using the new constructor {@link UserEntity#UserEntity(String, String, String)}.
     * @return List of {@link UserDTO} of migrated {@link UserEntity}
     */
    public List<UserDTO> migrateUsers() {
        List<UserEntity> usersToMigrate = this.userRepository.getByFirstNameIsNullOrLastNameIsNull();
        List<UserDTO> migratedUsers = new ArrayList<>();

        for (UserEntity userEntity : usersToMigrate) {
            userEntity.setFirstAndLastNameFromFullName();
            this.userRepository.save(userEntity);
            migratedUsers.add(new UserDTO(userEntity.getUsername(), userEntity.getFullName(), userEntity.getFirstName(), userEntity.getLastName()));
        }
        return migratedUsers;
    }

    /**
     * Utility function that returns an abstraction of a given {@link UserEntity} as {@link UserDTO} to {@link UserController} by version for {@link #findUser(String, String)}, {@link #createUser(String, String, UserDTO)} and {@link #updateUser(String, String, UserDTO)}.
     * @param userEntity {@link UserEntity} for abstraction
     * @param version specified resource representation version
     * @return {@link UserDTO} as abstraction of {@link UserEntity} for given version
     */
    private UserDTO getUserDtoFromUserEntityByVersion(UserEntity userEntity, String version) {
        if (version != null) {
            if (version.equals("1")) {
                return new UserDTO(userEntity.getUsername(), userEntity.getFullName());
            } else if (version.equals("2")) {
                return new UserDTO(userEntity.getUsername(), userEntity.getFirstName(), userEntity.getLastName());
            }
        }
        return new UserDTO(userEntity.getUsername(), userEntity.getFullName(), userEntity.getFirstName(), userEntity.getLastName());
    }
}
