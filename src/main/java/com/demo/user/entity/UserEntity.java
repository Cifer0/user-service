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
     * @param username identifying username
     * @param fullName entity attribute
     */
    public UserEntity(String username, String fullName) {
        this.username = username;
        this.fullName = fullName.trim().replaceAll(" +", " ");
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
}
