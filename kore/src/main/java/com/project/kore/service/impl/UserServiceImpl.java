package com.project.kore.service.impl;

import com.project.kore.enums.Role;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.User;
import com.project.kore.repository.UserRepository;
import com.project.kore.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/** Gestione utenti. Le query escludono sempre gli utenti soft-deleted; le password passano dal PasswordEncoder (BCrypt). */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Utente", id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new CustomResourceNotFoundException("Utente con email " + email + " non trovato."));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email).isPresent();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRoleAndDeletedFalse(role);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAllByDeletedFalse();
    }

    @Override
    public long countByAssignedPT(User pt) {
        return userRepository.countByAssignedPTAndDeletedFalse(pt);
    }

    @Override
    public long countByAssignedNutritionist(User nutritionist) {
        return userRepository.countByAssignedNutritionistAndDeletedFalse(nutritionist);
    }

    @Override
    public List<User> findByAssignedPT(User pt) {
        return userRepository.findByAssignedPTAndDeletedFalse(pt);
    }

    @Override
    public List<User> findByAssignedNutritionist(User nutritionist) {
        return userRepository.findByAssignedNutritionistAndDeletedFalse(nutritionist);
    }

    // Esclude l'utente stesso dal controllo: serve in update profilo, dove tenere la propria email non è un duplicato.
    @Override
    public boolean existsUserByEmailExcluding(String email, Long excludeId) {
        return userRepository.findByEmailAndIdIsNotAndDeletedFalse(email, excludeId).isPresent();
    }

    /**
     * Soft delete: marca l'utente come eliminato senza toglierlo dal DB. Se era un PT o un
     * nutrizionista, sgancia anche i client che gli erano assegnati, così non restano legati
     * a un professionista sparito.
     */
    @Override
    public void deleteUser(Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Utente", id));
        target.setDeleted(true);
        userRepository.save(target);

        if (target.getRole() == Role.PERSONAL_TRAINER) {
            userRepository.clearAssignedPT(id);
        } else if (target.getRole() == Role.NUTRITIONIST) {
            userRepository.clearAssignedNutritionist(id);
        }
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
