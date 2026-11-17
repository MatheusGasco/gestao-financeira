package com.gestaofinanceira.modules.auth;

import com.gestaofinanceira.modules.category.Category;
import com.gestaofinanceira.modules.category.CategoryRepository;
import com.gestaofinanceira.modules.category.CategoryType;
import com.gestaofinanceira.modules.user.Role;
import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.modules.user.UserRepository;
import com.gestaofinanceira.modules.verification.EmailVerificationService;
import com.gestaofinanceira.security.JwtTokenProvider;
import com.gestaofinanceira.shared.exception.BusinessException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;
    private final String googleClientId;

    public AuthService(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            AuthenticationManager authenticationManager,
            EmailVerificationService emailVerificationService,
            @Value("${google.client-id:}") String googleClientId
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.emailVerificationService = emailVerificationService;
        this.googleClientId = googleClientId;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        seedDefaultCategoriesForUser(user);

        emailVerificationService.createVerificationLink(user);

        return RegisterResponse.of(
                "Cadastro realizado! Confirme seu e-mail para liberar o acesso."
        );
    }

    
    @Transactional
    public void resendVerification(String email) {

        String normalizedEmail = email.toLowerCase().trim();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException(
                        "Não foi possível localizar a conta informada."
                ));

        emailVerificationService.resendVerificationLink(user);
    }


    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "E-mail ou senha inválidos."
                ));

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException(
                    "Confirme seu e-mail antes de fazer login."
            );
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        String token = tokenProvider.generateToken(user);

        return AuthResponse.of(token, UserDTO.fromEntity(user));
    }

    @Transactional
    public AuthResponse googleLogin(String credential) {

        if (googleClientId == null || googleClientId.isBlank()) {
            throw new BusinessException(
                    "Login com Google não está configurado no servidor."
            );
        }

        if (credential == null || credential.isBlank()) {
            throw new BusinessException(
                    "Credencial do Google não informada."
            );
        }

        GoogleIdToken.Payload payload;

        try {
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(),
                            GsonFactory.getDefaultInstance()
                    )
                    .setAudience(List.of(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);

            if (idToken == null) {
                throw new BusinessException(
                        "Credencial do Google inválida ou expirada."
                );
            }

            payload = idToken.getPayload();

        } catch (GeneralSecurityException | IOException e) {
            throw new BusinessException(
                    "Não foi possível validar a credencial do Google."
            );
        }

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new BusinessException(
                    "O Google não confirmou o endereço de e-mail desta conta."
            );
        }

        String googleId = payload.getSubject();
        String email = payload.getEmail().toLowerCase().trim();

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .orElse(null));

        if (user == null) {

            Object nameClaim = payload.get("name");

            String name = nameClaim != null
                    ? nameClaim.toString().trim()
                    : email.split("@")[0];

            if (name.isBlank()) {
                name = email.split("@")[0];
            }

            if (name.length() > 120) {
                name = name.substring(0, 120);
            }

            user = User.builder()
                    .name(name)
                    .email(email)
                    .googleId(googleId)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.ROLE_USER)
                    .emailVerified(true)
                    .build();

            user = userRepository.save(user);

            seedDefaultCategoriesForUser(user);

        } else {

            if (user.getGoogleId() != null
                    && !user.getGoogleId().equals(googleId)) {
                throw new BusinessException(
                        "Este e-mail já está vinculado a outra conta Google."
                );
            }

            user.setGoogleId(googleId);
            user.setEmailVerified(true);

            user = userRepository.save(user);
        }

        String token = tokenProvider.generateToken(user);

        return AuthResponse.of(token, UserDTO.fromEntity(user));
    }

    private void seedDefaultCategoriesForUser(User user) {

        List<Category> defaultCategories = List.of(
                Category.builder().user(user).name("Salário").type(CategoryType.INCOME).icon("briefcase").color("#10B981").build(),
                Category.builder().user(user).name("Rendimentos").type(CategoryType.INCOME).icon("trending-up").color("#059669").build(),
                Category.builder().user(user).name("Outras Receitas").type(CategoryType.INCOME).icon("plus-circle").color("#34D399").build(),
                Category.builder().user(user).name("Alimentação").type(CategoryType.EXPENSE).icon("utensils").color("#F59E0B").build(),
                Category.builder().user(user).name("Moradia").type(CategoryType.EXPENSE).icon("home").color("#EF4444").build(),
                Category.builder().user(user).name("Transporte").type(CategoryType.EXPENSE).icon("car").color("#6366F1").build(),
                Category.builder().user(user).name("Saúde").type(CategoryType.EXPENSE).icon("activity").color("#EC4899").build(),
                Category.builder().user(user).name("Lazer").type(CategoryType.EXPENSE).icon("film").color("#8B5CF6").build(),
                Category.builder().user(user).name("Educação").type(CategoryType.EXPENSE).icon("book-open").color("#3B82F6").build(),
                Category.builder().user(user).name("Investimentos").type(CategoryType.EXPENSE).icon("pie-chart").color("#06B6D4").build(),
                Category.builder().user(user).name("Outras Despesas").type(CategoryType.EXPENSE).icon("tag").color("#64748B").build()
        );

        categoryRepository.saveAll(defaultCategories);
    }
}