package com.project.kore.builder.impl;

import com.project.kore.builder.UserBuilder;
import com.project.kore.enums.Role;
import com.project.kore.util.BusinessConstants;
import java.time.LocalDateTime;
import java.util.Objects;
import com.project.kore.model.*;


/**
 * Builder concreto per User. In build controlla che email, password e role ci siano, che l'email sia in
 * formato valido e che la password rispetti la lunghezza minima.
 */
public class UserBuilderImpl implements UserBuilder {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private Long id;
    private String email;
    private String password;
    private String profilePicture;
    private String firstName;
    private String lastName;
    private Role role;
    private User assignedPT;
    private User assignedNutritionist;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public UserBuilder id(Long id) {
        this.id = id;
        return this;
    }
    @Override
    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }
    @Override
    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }
    @Override
    public UserBuilder profilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
        return this;
    }
    @Override
    public UserBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    @Override
    public UserBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
    @Override
    public UserBuilder role(Role role) {
        this.role = role;
        return this;
    }
    @Override
    public UserBuilder assignedPT(User assignedPT) {
        this.assignedPT = assignedPT;
        return this;
    }
    @Override
    public UserBuilder assignedNutritionist(User assignedNutritionist) {
        this.assignedNutritionist = assignedNutritionist;
        return this;
    }
    @Override
    public UserBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    @Override
    public UserBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Override
    public User build() {
        Objects.requireNonNull(this.email, "email è obbligatorio");
        Objects.requireNonNull(this.password, "password è obbligatorio");
        Objects.requireNonNull(this.role, "role è obbligatorio");

        if (this.email.isBlank())
            throw new IllegalArgumentException("email non può essere vuota");
        if (!this.email.matches(EMAIL_REGEX))
            throw new IllegalArgumentException("email non è un indirizzo valido: " + this.email);
        if (this.password.isBlank())
            throw new IllegalArgumentException("password non può essere vuota");
        if (this.password.length() < BusinessConstants.MIN_PASSWORD_LENGTH)
            throw new IllegalArgumentException("password deve contenere almeno " + BusinessConstants.MIN_PASSWORD_LENGTH + " caratteri");

        User obj = new User();
        obj.setId(this.id);
        obj.setEmail(this.email);
        obj.setPassword(this.password);
        obj.setProfilePicture(this.profilePicture);
        obj.setFirstName(this.firstName);
        obj.setLastName(this.lastName);
        obj.setRole(this.role);
        obj.setAssignedPT(this.assignedPT);
        obj.setAssignedNutritionist(this.assignedNutritionist);
        obj.setCreatedAt(this.createdAt);
        obj.setUpdatedAt(this.updatedAt);
        return obj;
    }
}
