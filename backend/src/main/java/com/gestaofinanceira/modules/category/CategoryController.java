package com.gestaofinanceira.modules.category;

import com.gestaofinanceira.modules.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO.Response>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.listAllAvailable(user));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO.Response> create(
            @Valid @RequestBody CategoryDTO.Request request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request, user));
    }
}
