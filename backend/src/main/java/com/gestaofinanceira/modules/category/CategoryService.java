package com.gestaofinanceira.modules.category;

import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTO.Response> listAllAvailable(User user) {
        return categoryRepository.findAllAvailableForUser(user)
                .stream()
                .map(CategoryDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public Category findEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));
    }

    @Transactional
    public CategoryDTO.Response create(CategoryDTO.Request request, User user) {
        Category category = Category.builder()
                .user(user)
                .name(request.getName().trim())
                .type(request.getType())
                .icon(request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : "tag")
                .color(request.getColor() != null && !request.getColor().isBlank() ? request.getColor() : "#64748B")
                .isDefault(false)
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryDTO.Response.fromEntity(saved);
    }
}
