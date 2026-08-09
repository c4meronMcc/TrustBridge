package com.trustbridge.Features.Auth;

import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the user details by the provided email address.
     * This method retrieves the user from the database using the given email,
     * and constructs a {@link UserDetails} object
     * containing the user's email, password, and roles.
     *
     * @param email the email address of the user to load
     * @return a {@link UserDetails} instance containing user credentials and authorities
     * @throws UsernameNotFoundException if no user is found with the provided email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword()) // Is this 'getPassword()' or 'getPasswordHash()'?
                .authorities(user.getUserRole().name())
                .build();
    }
}
