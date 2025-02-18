package com.example.Shares.auth.service.auth;


import com.example.Shares.auth.bo.CustomUserDetails;
import com.example.Shares.auth.entity.UserEntity;
import com.example.Shares.auth.exception.UserNotFoundException;
import com.example.Shares.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found for username: " + username));
        return new CustomUserDetails(user); // Use the new constructor
    }


    // Load user by civil ID
    public UserDetails loadUserByCivilId(String civilId) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByCivilId(civilId)
                .orElseThrow(() -> new UserNotFoundException("User not found for Civil ID: " + civilId));

        return new CustomUserDetails(user);
    }


//    private CustomUserDetails buildCustomUserDetailsOfUsername(String username) {
//        UserEntity user =
//                userRepository
//                        .findByUsernameIgnoreCase(username)
//                        .orElseThrow(() -> new UserNotFoundException("Incorrect Username"));
//
//        CustomUserDetails userDetails = new CustomUserDetails();
//        userDetails.setId(user.getId());
//        userDetails.setUserName(user.getUsername());
//        userDetails.setPassword(user.getPassword());
//        userDetails.setRole(user.getRole().toString());
//
//
//        return userDetails;
//    }
}