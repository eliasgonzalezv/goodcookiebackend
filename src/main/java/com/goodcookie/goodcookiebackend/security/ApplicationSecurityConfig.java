package com.goodcookie.goodcookiebackend.security;

import com.goodcookie.goodcookiebackend.jwt.JwtAuthenticationFilter;
import com.goodcookie.goodcookiebackend.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * This class serves as the configuration for all the security features of this
 * spring application.
 */
@Configuration
@EnableWebSecurity
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public ApplicationSecurityConfig(UserDetailsServiceImpl userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                //enable cors
                .cors().and()
                .csrf().disable()
                //Ensure session is stateless
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //Register authentication filters
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                //Public endpoints
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/resource/phrase" ).permitAll()
                //Private endpoints
                .antMatchers(HttpMethod.GET, "/api/resource/getJournals").authenticated()
                .antMatchers(HttpMethod.POST, "/api/resource/saveJournal").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/resource/deleteJournal").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/resource/deleteJournals/**").authenticated()
                //Authenticate any other type of request
                .anyRequest()
                .authenticated();
    }

    @Autowired
    //Register the UserDetailsService implementation for spring to use
    protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    //Register authentication manager bean for spring to autowire
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    //Register the password encoder using BCrypt hashing algorithm
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }


}
