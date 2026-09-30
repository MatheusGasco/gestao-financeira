package com.gestaofinanceira.modules.auth;

import com.gestaofinanceira.modules.category.Category;
import com.gestaofinanceira.modules.category.CategoryRepository;
import com.gestaofinanceira.modules.category.CategoryType;
import com.gestaofinanceira.modules.user.Role;
import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.modules.user.UserRepository;
import com.gestaofinanceira.security.JwtTokenProvider;
import com.gestaofinanceira.shared.exception.BusinessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        user = userRepository.save(user);

        // Criar categorias padrão para o novo usuário
        seedDefaultCategoriesForUser(user);

        String token = tokenProvider.generateToken(user);
        return AuthResponse.of(token, UserDTO.fromEntity(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos."));

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
