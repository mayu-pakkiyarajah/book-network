package com.mayu.book.auth;

import com.mayu.book.email.EmailService;
import com.mayu.book.email.EmailTemplateName;
import com.mayu.book.role.RoleRepository;
import com.mayu.book.security.JwtService;
import com.mayu.book.user.Token;
import com.mayu.book.user.TokenRepository;
import com.mayu.book.user.User;
import com.mayu.book.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("http://localhost:4200/activate-account")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {

        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("User not found"));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.firstName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );

    }

    private String generateAndSaveActivationToken(User user) {

        //Email verification code generation
        String generatedToken = activationToken(6);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .user(user)
                .build();

        tokenRepository.save(token);

        return generatedToken;
    }

    private String activationToken(int length) {

        String characters = "0123456789";
        StringBuilder tokenBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            tokenBuilder.append(characters.charAt(randomIndex));
        }

        return tokenBuilder.toString();

    }

    public AuthenticationResponse authenticate(@Valid AuthenticationRequest request) {

        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        var user = ((User)auth.getPrincipal());
        claims.put("firstName", user.firstName());

        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
         Token savedToken = tokenRepository.findByToken(token)
                 .orElseThrow(() -> new RuntimeException("Token not found or Invalid Token"));

         if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
             sendValidationEmail(savedToken.getUser());
             throw new MessagingException("Token is expired  & New token has been sent");
         }

         var user = userRepository.findById(savedToken.getUser().getId())
                 .orElseThrow(() -> new UsernameNotFoundException("User not found"));

         user.setEnabled(true);
         userRepository.save(user);

         savedToken.setValidatedAt(LocalDateTime.now());
         tokenRepository.save(savedToken);
    }
}
