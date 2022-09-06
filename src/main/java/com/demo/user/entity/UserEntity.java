package com.demo.user.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "user_data")
public class UserEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    @Size(min = 3, max = 20)
    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @CreationTimestamp
    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date creationTime = new Date();

    @UpdateTimestamp
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    protected UserEntity() {

    }

    /**
     * Constructor for version = 1.
     * Sets {@link UserEntity#firstName} and {@link UserEntity#lastName} for data integrity.
     * @param username identifying username
     * @param fullName entity attribute
     */
    public UserEntity(String username, String fullName) {
        this.username = username;
        this.fullName = fullName.trim().replaceAll(" +", " ");
        this.setFirstAndLastNameFromFullName();
    }

    /**
     * Constructor for latest version.
     * Sets {@link UserEntity#fullName} for backward-compatibility with older version.
     * @param username identifying username
     * @param firstName entity attribute
     * @param lastName entity attribute
     */
    public UserEntity(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName.trim().replaceAll(" +", " ");
        this.lastName = lastName.trim().replaceAll(" +", " ");
        this.setFullNameFromFirstAndLastName();
    }

    public UUID getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFullName() {
        return this.fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName.trim().replaceAll(" +", " ");
        setFirstAndLastNameFromFullName();
    }

    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName.trim().replaceAll(" +", " ");
        setFullNameFromFirstAndLastName();
    }

    public String getLastName() {
        return this.lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName.trim().replaceAll(" +", " ");
        setFullNameFromFirstAndLastName();
    }

    public Date getCreationTime() {
        return this.creationTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    /**
     * Updates {@link UserEntity#updateTime} entry for every write on the entity.
     */
    @PrePersist
    @PreUpdate
    public void setUpdateTime() {
        this.updateTime = new Date();
    }

    /**
     * Sets {@link UserEntity#fullName} by combining {@link UserEntity#firstName} and {@link UserEntity#lastName}.
     */
    private void setFullNameFromFirstAndLastName() {
        this.fullName = this.firstName + " " + this.lastName;
    }

    /**
     * Sets {@link UserEntity#firstName} and {@link UserEntity#lastName} by splitting {@link UserEntity#fullName}.
     */
    public void setFirstAndLastNameFromFullName() {
        final int index = this.fullName.lastIndexOf(" ");
        System.out.println(index);
        this.firstName = index > -1 ? this.fullName.substring(0, index) : this.fullName;
        this.lastName = index > -1 ? this.fullName.substring(index + 1) : null;
    }
}
