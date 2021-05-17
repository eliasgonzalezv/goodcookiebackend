package com.goodcookie.goodcookiebackend.jwt;

import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.security.SecurityConstantsConfig;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Contains the authentication logic to validate
 * incoming user credentials with the request to login.
 * Checks whether JWT is still valid or not
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final SecurityConstantsConfig securityConstantsConfig;


    @Autowired
    public JwtAuthenticationFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil, SecurityConstantsConfig securityConstantsConfig) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.securityConstantsConfig = securityConstantsConfig;
    }

    @Override
    //Intercepts the requests and checks the authorization header
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(securityConstantsConfig.getHeaderName());
        //Check if header is valid
        if(authorizationHeader == null
                || !authorizationHeader.startsWith(securityConstantsConfig.getTokenPrefix())){
            //Reject request
            filterChain.doFilter(request, response);
            return;
        }

        try{
            //Get the token from client
           String token = authorizationHeader.replace(securityConstantsConfig.getTokenPrefix(), "");
           //Validate the token
            if(StringUtils.hasText(token) && this.jwtUtil.validateToken(token)) {
                //Parse the token
                String username = jwtUtil.extractUsername(token);

                //Fetch the user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //Set the authentication token for spring to validate
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        userDetails.getAuthorities()
                );


                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //Set the authentication in the context to specify that the current user is authenticated
                //So it passes the Spring Configurations  successfully
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }else{
                throw new GoodCookieBackendException("Cannot set the Security Context");
            }
        }
        catch (JwtException e){
            //Token is not valid
            throw new GoodCookieBackendException("Token Provided cannot be trusted");
        }
        //Pass request and response onto the next filter in the chain
        filterChain.doFilter(request, response);

    }
}
