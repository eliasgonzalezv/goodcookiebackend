package com.goodcookie.goodcookiebackend.service;

import com.goodcookie.goodcookiebackend.model.User;
import com.goodcookie.goodcookiebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;


/**
 * Implementation of the UserDetailsServices interface
 * provided by spring security to automatically loadUsers with thei
 * authority.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Find the user information related to the provided username
        Optional<User> userOptional = userRepository.findByUsername(username);

        //Attempt to construct an user object with the credentials obtained from the DB
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("No user "+
                "found with username: " + username));

        //Return an User for spring to authenticate
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                //Enabled
                true,
                //accountNonExpired
                true,
                //CredentialsNonExpired
                true,
                //accountNonLocked
                true,
                getAuthorities());
    }

    /**
     * Returns a list containing the User authority
     * @return A singletonList containing the User authority
     */
    private Collection<? extends GrantedAuthority> getAuthorities(){
        return Collections.singletonList(new SimpleGrantedAuthority("USER"));
    }

}
