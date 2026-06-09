package com.project.kore.model;

import com.project.kore.enums.Role;
import com.project.kore.util.BusinessConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Un utente della piattaforma, in qualunque ruolo. Implementa {@link UserDetails}, quindi
 * vive anche come principal di Spring Security: l'email fa da username e un account con
 * soft-delete non riesce più ad autenticarsi.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_email", columnNames = {"email"})
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Version: optimistic locking, gestito da JPA
    @Version
    private Integer version;

    // L'email è anche lo username: deve restare univoca
    @NotBlank(message = "email non può essere vuota")
    @Pattern(regexp = BusinessConstants.EMAIL_REGEX, message = "email non è un indirizzo valido")
    @Column(nullable = false)
    private String email;

    // Hash BCrypt, mai la password in chiaro
    @Column(nullable = false)
    private String password;

    // Base64 o URL dell'immagine, può essere null
    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    private String firstName;
    private String lastName;

    @NotNull(message = "role è obbligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // PT e nutrizionista del cliente: null per chi non è un CLIENT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_pt_id", foreignKey = @ForeignKey(name = "fk_user_assigned_pt_id"))
    private User assignedPT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_nutritionist_id", foreignKey = @ForeignKey(name = "fk_user_assigned_nutritionist_id"))
    private User assignedNutritionist;

    // Soft-delete: l'account resta in DB ma non può più autenticarsi
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public User getAssignedPT() { return assignedPT; }
    public void setAssignedPT(User assignedPT) { this.assignedPT = assignedPT; }

    public User getAssignedNutritionist() { return assignedNutritionist; }
    public void setAssignedNutritionist(User assignedNutritionist) { this.assignedNutritionist = assignedNutritionist; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Spring Security vuole le authority nel formato ROLE_<RUOLO>
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Non c'è uno username dedicato: usiamo l'email
    @Override
    public String getUsername() {
        return email;
    }

    // Un account soft-deleted risulta disabilitato e non può loggarsi
    @Override
    public boolean isEnabled() {
        return !deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', firstName='" + firstName + "', lastName='" + lastName + "', role=" + role + "}";
    }
}
