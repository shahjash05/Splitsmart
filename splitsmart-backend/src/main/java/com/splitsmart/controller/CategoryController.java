package com.splitsmart.controller;

import com.splitsmart.dto.response.CategoryDTO;
import com.splitsmart.model.Category;
import com.splitsmart.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll() {
        List<CategoryDTO> categories = categoryRepository.findAll().stream()
                .map(c -> CategoryDTO.builder()
                        .categoryId(c.getCategoryId())
                        .categoryName(c.getCategoryName())
                        .icon(c.getIcon())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}
