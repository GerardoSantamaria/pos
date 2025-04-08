package com.pos.config;

import com.pos.models.Category;
import com.pos.models.Product;
import com.pos.models.User;
import com.pos.repositories.CategoryRepository;
import com.pos.repositories.ProductRepository;
import com.pos.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Component
@Profile("dev")  // Solo se ejecuta en perfil de desarrollo
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor con inyección de dependencias en lugar de @RequiredArgsConstructor
    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository,
                           ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void initData() {
        if (userRepository.count() > 0) {
            log.info("La base de datos ya está inicializada, saltando inicialización");
            return;
        }

        log.info("Inicializando datos de demostración...");
        initUsers();
        initCategoriesAndProducts();
        log.info("Datos de demostración inicializados correctamente");
    }

    private void initUsers() {
        // Crear usuarios de demostración
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setFullName("Administrador");
        admin.setRoles(Set.of("ADMIN", "CASHIER"));
        admin.setActive(true);
        userRepository.save(admin);

        User cashier = new User();
        cashier.setUsername("cajero");
        cashier.setPassword(passwordEncoder.encode("cajero"));
        cashier.setFullName("Cajero Demo");
        cashier.setRoles(Set.of("CASHIER"));
        cashier.setActive(true);
        userRepository.save(cashier);

        log.info("Usuarios creados: {}", userRepository.count());
    }

    private void initCategoriesAndProducts() {
        // Crear categorías
        Category beverages = new Category();
        beverages.setName("Bebidas");
        beverages.setDescription("Refrescos, agua, jugos y bebidas alcohólicas");
        categoryRepository.save(beverages);

        Category snacks = new Category();
        snacks.setName("Botanas");
        snacks.setDescription("Frituras, galletas, dulces y snacks varios");
        categoryRepository.save(snacks);

        Category dairy = new Category();
        dairy.setName("Lácteos");
        dairy.setDescription("Leche, queso, yogurt y derivados lácteos");
        categoryRepository.save(dairy);

        Category groceries = new Category();
        groceries.setName("Abarrotes");
        groceries.setDescription("Productos básicos y alimentos no perecederos");
        categoryRepository.save(groceries);

        // Crear productos de ejemplo
        createProduct("7501055304172", "COCA001", "Coca-Cola 600ml", "Refresco Coca-Cola 600ml", new BigDecimal("18.50"), 50, beverages);
        createProduct("7501055304189", "COCA002", "Coca-Cola 1L", "Refresco Coca-Cola 1 litro", new BigDecimal("24.00"), 40, beverages);
        createProduct("7501055304196", "COCA003", "Coca-Cola 2L", "Refresco Coca-Cola 2 litros", new BigDecimal("32.50"), 30, beverages);

        createProduct("7501000611690", "SABN001", "Sabritas Naturales 45g", "Papas fritas Sabritas Naturales 45g", new BigDecimal("15.00"), 60, snacks);
        createProduct("7501000611706", "SABN002", "Sabritas Adobadas 45g", "Papas fritas Sabritas Adobadas 45g", new BigDecimal("15.00"), 55, snacks);
        createProduct("7501000611713", "DORITOS01", "Doritos Nacho 58g", "Botana Doritos Nacho 58g", new BigDecimal("17.50"), 45, snacks);

        createProduct("7501055910014", "LECHE001", "Leche Alpura Entera 1L", "Leche entera ultrapasteurizada Alpura 1 litro", new BigDecimal("25.50"), 35, dairy);
        createProduct("7501055910021", "LECHE002", "Leche Alpura Light 1L", "Leche light ultrapasteurizada Alpura 1 litro", new BigDecimal("26.50"), 30, dairy);

        createProduct("7501003340019", "MASC001", "Maseca 1Kg", "Harina de maíz Maseca 1Kg", new BigDecimal("20.50"), 40, groceries);
        createProduct("7501000120057", "AZUC001", "Azúcar Estándar 1Kg", "Azúcar estándar 1Kg", new BigDecimal("32.00"), 25, groceries);

        log.info("Categorías creadas: {}", categoryRepository.count());
        log.info("Productos creados: {}", productRepository.count());
    }

    private void createProduct(String barcode, String sku, String name, String description,
                               BigDecimal price, Integer stock, Category category) {
        Product product = new Product();
        product.setBarcode(barcode);
        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setActive(true);

        productRepository.save(product);
    }
}