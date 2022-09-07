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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "name_id", referencedColumnName = "id")
    private NameEntity nameEntity;

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
     * Constructor for latest version.
     * Creates and sets foreign key to {@link NameEntity}.
     * Performs Duplicate Write to ensure data integrity.
     * @param username identifying username
     * @param firstName entity attribute
     * @param lastName entity attribute
     */
    public UserEntity(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName.trim().replaceAll(" +", " ");
        this.lastName = lastName.trim().replaceAll(" +", " ");
        this.nameEntity = new NameEntity(firstName, lastName);
    }

    public UUID getId() {
        return this.id;
    }

    public NameEntity getNameEntity() {
        return this.nameEntity;
    }
    public void setNameEntity(NameEntity nameEntity) {
        this.nameEntity = nameEntity;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFirstName() {
        return this.firstName;
    }
    /**
     * Performs Duplicate Write to ensure data integrity.
     * @param firstName entity attribute
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName.trim().replaceAll(" +", " ");
        this.nameEntity.setFirstName(firstName);
    }

    public String getLastName() {
        return this.lastName;
    }
    /**
     * Performs Duplicate Write to ensure data integrity.
     * @param lastName entity attribute
     */
    public void setLastName(String lastName) {
        this.lastName = lastName.trim().replaceAll(" +", " ");
        this.nameEntity.setLastName(lastName);
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
