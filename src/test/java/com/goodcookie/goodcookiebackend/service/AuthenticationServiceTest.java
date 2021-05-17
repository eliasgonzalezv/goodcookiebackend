package com.goodcookie.goodcookiebackend.service;

import com.goodcookie.goodcookiebackend.dto.AuthenticationRequest;
import com.goodcookie.goodcookiebackend.dto.AuthenticationResponse;
import com.goodcookie.goodcookiebackend.dto.RefreshTokenRequest;
import com.goodcookie.goodcookiebackend.dto.UserDto;
import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.jwt.JwtUtil;
import com.goodcookie.goodcookiebackend.model.RefreshToken;
import com.goodcookie.goodcookiebackend.model.User;
import com.goodcookie.goodcookiebackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * Testing class for AuthenticationService
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RefreshTokenService refreshTokenService;
    private UserDto userDto;
    private AuthenticationService underTest;

    @BeforeEach
    void setUp() {
        userDto = new UserDto("test122","test123@gmail.com",
                "test122test");
        underTest = new AuthenticationService(passwordEncoder, userRepository, authenticationManager, jwtUtil, refreshTokenService);
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void canRegisterANewUser() {
        //given
        System.out.println("canRegisterANewUser Test: Begin");
        User userFromDto = new User();
        userFromDto.setUsername(userDto.getUsername());
        userFromDto.setEmail(userDto.getEmail());
        userFromDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        //when
        underTest.registerUser(userDto);
        //then
        ArgumentCaptor<User> userArgumentCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userArgumentCaptor.capture());

        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser).isEqualTo(userFromDto);

        System.out.println("canRegisterANewUser Test: Pass");
    }

    @Test
    void shouldNotRegisterAnUserWhenUsernameAlreadyExists(){
        //given
//        UserDto userDto = new UserDto("test122","test123@gmail.com",
//                "test122test");

        System.out.println("shouldNotRegisterAnUserWhenUsernameAlreadyExists Test: Begin");
        given(userRepository.existsByUsername(userDto.getUsername())).willReturn(true);
        //when
        //then
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Username already exists.", HttpStatus.CONFLICT);
        assertThat(underTest.registerUser(userDto)).isEqualTo(expectedResponse);
        System.out.println("shouldNotRegisterAnUserWhenEmailAlreadyExist Test: Pass");
    }

    @Test
    void shouldNotRegisterAnUserWhenEmailAlreadyExists(){
        //given
//        UserDto userDto = new UserDto("test122","test123@gmail.com",
//                "test122test");
        System.out.println("shouldNotRegisterAnUserWhenEmailAlreadyExists Test: Begin");

        given(userRepository.existsByEmail(userDto.getEmail())).willReturn(true);
        //when
        //then
        ResponseEntity<String> expectedResponse =
                new ResponseEntity<>("Email already in use.", HttpStatus.CONFLICT);
        assertThat(underTest.registerUser(userDto)).isEqualTo(expectedResponse);
        //Only gets executed if the assert succeeds
        System.out.println("shouldNotRegisterAnUserWhenEmailAlreadyExist Test: Pass");

    }

    @Test
    void canLoginAnUser(){
        System.out.println("canLoginAnUser test: Begin");
        //given
        AuthenticationRequest authenticationRequest =
                AuthenticationRequest.builder()
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .build();
        //when
        Authentication authMock = mock(Authentication.class);
        RefreshToken refreshTokenMock = mock(RefreshToken.class);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword());
        when(
                authenticationManager.authenticate(usernamePasswordAuthenticationToken))
                .thenReturn(authMock);
        when(jwtUtil.generateToken(authMock)).thenReturn("token");
        when(refreshTokenService.generateRefreshToken()).thenReturn(refreshTokenMock);
        //then
        assertThat(underTest.login(authenticationRequest)).isInstanceOf(AuthenticationResponse.class);

        System.out.println("canLoginAnUser test: Pass");



    }

    @Test
    void shouldThrowWhenLoginAnUserWithBadCredentials(){
        System.out.println("shouldThrowWhenLoginAnUserWithBadCredentials Test: Begin");
        AuthenticationRequest authenticationRequest =
                AuthenticationRequest.builder()
                        .username(userDto.getUsername())
                        .password(userDto.getPassword())
                        .build();

        Authentication authMock = mock(Authentication.class);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword());

        //When
        //Then
        Throwable exception = Assertions.assertThrows(GoodCookieBackendException.class, () -> {
           when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenThrow(BadCredentialsException.class);
           then(underTest.login(authenticationRequest)); });
        Assertions.assertEquals("Incorrect username or password", exception.getMessage());

        System.out.println("shouldThrowWhenLoginAnUserWithBadCredentials Test: Pass");
    }

    @Test
    void canReturnRefreshToken() {
        System.out.println("canReturnRefreshToken Test: Begin");

        //given
        RefreshTokenRequest refreshTokenRequest =
                new RefreshTokenRequest(UUID.randomUUID().toString(),userDto.getUsername());
        given(refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken())).willReturn(true);
        //when
        underTest.refreshToken(refreshTokenRequest);
        when(jwtUtil.generateTokenWithUsername(refreshTokenRequest.getUsername())).thenReturn("token");
        //then
        assertThat(underTest.refreshToken(refreshTokenRequest)).isInstanceOf(AuthenticationResponse.class);

        System.out.println("canReturnRefreshToken Test: Pass");
    }
    @Test
    void shouldThrowWhenInvalidRefreshToken() {
        System.out.println("shouldThrowWhenInvalidRefreshToken Test: Begin");
        //Given
        RefreshTokenRequest refreshTokenRequest =
                new RefreshTokenRequest(UUID.randomUUID().toString(),userDto.getUsername());

        given(refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken())).willReturn(false);
        //When
        //Then
        Throwable exception = Assertions.assertThrows(GoodCookieBackendException.class,
                () -> underTest.refreshToken(refreshTokenRequest));
        Assertions.assertEquals("Invalid RefreshToken", exception.getMessage());
        System.out.println("shouldThrowWhenLoginAnUserWithBadCredentials Test: Pass");
    }
}