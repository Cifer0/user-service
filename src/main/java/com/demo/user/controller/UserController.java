package com.demo.user.controller;

import com.demo.user.dto.UserDTO;
import com.demo.user.entity.UserEntity;
import com.demo.user.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Presentation layer of the user-service.
 * Responsible for taking requests and checking them for validity and requested version before passing the content to {@link UserService}.
 */
@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Calls {@link UserService#findUser(String, String)}.
     * @param username identifying username as path variable
     * @param version specified version as request parameter
     * @return {@link ResponseEntity} with {@link HttpStatus}-Code:
     *      200 with {@link UserDTO} of {@link UserEntity} with given username
     *      404 if {@link UserEntity} with given username does not exist
     */
    @GetMapping(value = "user/{username}")
    public ResponseEntity<UserDTO> getUser(@PathVariable("username") final String username, @RequestParam(required = false) final String version) {
        Optional<UserDTO> optUserDTO = this.userService.findUser(username, version);

        if (optUserDTO.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(optUserDTO.get(), HttpStatus.OK);
    }

    /**
     * Checks request for validity and (perceived) version before calling {@link UserService#createUser(String, String, UserDTO)}.
     * A valid username begins with a letter, is alphanumeric, with a minimum length of 3 and a maximum length of 20.
     * @param username identifying username as path variable
     * @param version specified version as request parameter
     * @param userDTO appended user data as request body
     * @return {@link ResponseEntity} with {@link HttpStatus}-Code:
     *      200 with {@link UserDTO} of the created {@link UserEntity} if successful
     *      403 if username is invalid or {@link UserEntity} with given username already exist
     *      400 if request body is not valid
     */
    @PostMapping(value = "user/{username}")
    public ResponseEntity<UserDTO> postUser(@PathVariable("username") final String username, @RequestParam(required = false) final String version, @RequestBody final UserDTO userDTO) {
        // check for valid username (beginning with a letter, alphanumeric, minimum length: 3, maximum length: 20)
        String usernameRegex = "^([a-zA-Z])+([\\w]{2,19})+$";
        Pattern usernamePattern = Pattern.compile(usernameRegex);
        if (!usernamePattern.matcher(username).matches()) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        String perceivedVersion = validateRequestByVersion("POST", username, version, userDTO);

        if (perceivedVersion.equals("-1")) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final Optional<UserDTO> optUserDTO = this.userService.createUser(username, perceivedVersion, userDTO);

        if (optUserDTO.isEmpty()) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("user/" + username));

        return new ResponseEntity<>(optUserDTO.get(), headers, HttpStatus.CREATED);
    }

    /**
     * Checks request for validity and (perceived) version before calling {@link UserService#updateUser(String, String, UserDTO)}.
     * @param username identifying username as path variable
     * @param version specified version as request parameter
     * @param userDTO appended user data as request body
     * @return {@link ResponseEntity} with {@link HttpStatus}-Code:
     *      200 with {@link UserDTO} of the updated {@link UserEntity} if successful
     *      404 if {@link UserEntity} with given username does not exist
     *      400 if request body is not valid
     */
    @PutMapping(value = "user/{username}")
    public ResponseEntity<UserDTO> putUser(@PathVariable("username") final String username, @RequestParam(required = false) final String version, @RequestBody final UserDTO userDTO) {
        String perceivedVersion = validateRequestByVersion("PUT", username, version, userDTO);

        if (perceivedVersion.equals("-1")) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final Optional<UserDTO> optUserDTO = this.userService.updateUser(username, perceivedVersion, userDTO);

        if (optUserDTO.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(optUserDTO.get(), HttpStatus.OK);
    }

    /**
     * Deletes {@link UserEntity} by given username and returns a {@link UserDTO} of the deleted {@link UserEntity}.
     * @param username identifying username for deletion of corresponding {@link UserEntity} as path variable
     * @return {@link ResponseEntity} with {@link HttpStatus}-Code:
     *      200 with {@link UserDTO} of deleted {@link UserEntity} if successful
     *      404 if {@link UserEntity} with given username does not exist
     */
    @DeleteMapping(value = "user/{username}")
    public ResponseEntity<UserDTO> deleteUser(@PathVariable("username") final String username, @RequestParam(required = false) final String version) {
        final Optional<UserDTO> optUserDTO = this.userService.deleteUser(username, version);

        if (optUserDTO.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(optUserDTO.get(), HttpStatus.OK);
    }

    /**
     * Utility function that determines the (perceived) version of the request and the validity of a given request body by version for {@link #postUser(String, String, UserDTO)} and {@link #putUser(String, String, UserDTO)}.
     * Allows for detection of the perceived resource representation version by the content of the request, in case none was specified as a request parameter.
     * The fullName-attribute has to contain a whitespace.
     * Using a perceived version, the lower layers don't have to seek out the version again.
     * @param requestType POST- or PUT-request
     * @param username identifying username as path variable
     * @param version requested resource representation version as request parameter; might be null
     * @param userDTO appended user data as request body
     * @return String with either -1 for invalid request body or the perceived version number
     */
    private String validateRequestByVersion(String requestType, String username, String version, UserDTO userDTO) {
        String perceivedVersion = "-1";
        // fullName needs to contain a whitespace in the middle
        String fullNameRegex = "\\s";
        Pattern fullNamePattern = Pattern.compile(fullNameRegex);

        if (userDTO.getUsername() != null && !userDTO.getUsername().equals(username)) return perceivedVersion;

        final boolean fullNameIsNull = userDTO.getFullName() == null;

        if (requestType.equals("POST") && fullNameIsNull) {
            return perceivedVersion;
        } else {
            if (!fullNamePattern.matcher(userDTO.getFullName().trim()).find()) return perceivedVersion;
        }
        perceivedVersion = "1";

        return perceivedVersion;
    }
}
