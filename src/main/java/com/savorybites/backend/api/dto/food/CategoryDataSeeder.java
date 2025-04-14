package com.savorybites.backend.api.dto.food;

import com.savorybites.backend.model.Category;
import com.savorybites.backend.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CategoryDataSeeder {

    private final CategoryRepository categoryRepository;

    public CategoryDataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                    create("All", null, "🍽"),
                    create("Burger", "https://example.com/img/burger.jpg", "🍔"),
                    create("Plate", "https://example.com/img/plate.jpg", "🍛"),
                    create("Dessert", "https://example.com/img/dessert.jpg", "🍰"),
                    create("Pasta", "https://example.com/img/pasta.jpg", "🍝"),
                    create("Moroccan Food", "https://example.com/img/moroccan.jpg", "🇲🇦"),
                    create("Pizza", "https://example.com/img/pizza.jpg", "🍕"),
                    create("Sandwich", "https://example.com/img/sandwich.jpg", "🥪"),
                    create("Soup", "https://example.com/img/soup.jpg", "🥣"),
                    create("Chicken", "https://example.com/img/chicken.jpg", "🍗"),
                    create("Vegan", "https://example.com/img/vegan.jpg", "🥗"),
                    create("Beverages", "https://example.com/img/drinks.jpg", "🥤"),
                    create("Seafood", "https://example.com/img/seafood.jpg", "🦐"),
                    create("Fries", "https://example.com/img/fries.jpg", "🍟"),
                    create("Breakfast", "https://example.com/img/breakfast.jpg", "🍳"),
                    create("Sushi", "https://example.com/img/sushi.jpg", "🍣")
            );

            categoryRepository.saveAll(categories);
            System.out.println("✅ Categories seeded successfully.");
        }
    }

    private Category create(String title, String image, String icon) {
        Category c = new Category();
        c.setTitle(title);
        c.setCategoryImage(image);
        c.setCategoryIcon(icon);
        return c;
    }
}
