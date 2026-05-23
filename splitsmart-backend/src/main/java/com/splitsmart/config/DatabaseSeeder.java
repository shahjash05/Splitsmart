package com.splitsmart.config;

import com.splitsmart.model.Category;
import com.splitsmart.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DatabaseSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                Category.builder().categoryName("Food & Dining").icon("🍔").build(),
                Category.builder().categoryName("Transportation").icon("🚗").build(),
                Category.builder().categoryName("Entertainment").icon("🎬").build(),
                Category.builder().categoryName("Utilities").icon("💡").build(),
                Category.builder().categoryName("Shopping").icon("🛍️").build(),
                Category.builder().categoryName("Health").icon("🏥").build(),
                Category.builder().categoryName("Travel").icon("✈️").build(),
                Category.builder().categoryName("Other").icon("📦").build()
            ));
            System.out.println("DatabaseSeeder: Successfully seeded default categories.");
        }
    }
}
