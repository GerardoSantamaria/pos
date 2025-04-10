package com.pos.config;

import com.pos.models.Role;
import com.pos.models.User;
import com.pos.models.Product;
import com.pos.repositories.RoleRepository;
import com.pos.repositories.UserRepository;
import com.pos.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Component to initialize the database with sample data.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor with dependencies.
     *
     * @param roleRepository The role repository
     * @param userRepository The user repository
     * @param productRepository The product repository
     * @param passwordEncoder The password encoder
     */
    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Initializes the database with roles, users, and sample products.
     *
     * @param args Command line arguments
     */
    @Override
    @Transactional
    public void run(String... args) {
        // Only initialize if data doesn't exist
        if (roleRepository.count() > 0) {
            return;
        }

        // Create roles
        Role adminRole = createRole("ROLE_ADMIN");
        Role vendorRole = createRole("ROLE_VENDEDOR");

        // Create admin user
        createUserWithRoles("admin", "admin", true, adminRole);

        // Create vendor user
        createUserWithRoles("vendedor", "vendedor", true, vendorRole);

        // Create sample products
        createSampleProducts();
    }

    /**
     * Creates a role.
     *
     * @param name The role name
     * @return The created role
     */
    private Role createRole(String name) {
        Role role = new Role(name);
        return roleRepository.save(role);
    }

    /**
     * Creates a user with the specified roles.
     *
     * @param username The username
     * @param password The password
     * @param enabled Whether the user is enabled
     * @param roles The roles to assign
     * @return The created user
     */
    private User createUserWithRoles(String username, String password, boolean enabled, Role... roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(enabled);

        Set<Role> roleSet = new HashSet<>();
        for (Role role : roles) {
            roleSet.add(role);
        }
        user.setRoles(roleSet);

        return userRepository.save(user);
    }

    /**
     * Creates sample products.
     */
    private void createSampleProducts() {
        // Create sample products
        createProduct("1234567890123", "Laptop HP ProBook", "Laptop de alta gama para uso profesional", new BigDecimal("999.99"), 10);
        createProduct("2345678901234", "Monitor LG 24\"", "Monitor LED Full HD", new BigDecimal("199.99"), 15);
        createProduct("3456789012345", "Teclado Mecánico Logitech", "Teclado mecánico con retroiluminación RGB", new BigDecimal("89.99"), 20);
        createProduct("4567890123456", "Mouse Inalámbrico", "Mouse ergonómico inalámbrico", new BigDecimal("29.99"), 30);
        createProduct("5678901234567", "Disco Duro SSD 1TB", "Disco de estado sólido de alta velocidad", new BigDecimal("149.99"), 25);
        createProduct("6789012345678", "Memoria RAM 16GB", "Memoria RAM DDR4 de alta velocidad", new BigDecimal("79.99"), 40);
        createProduct("7890123456789", "Impresora HP LaserJet", "Impresora láser monocromática", new BigDecimal("249.99"), 8);
        createProduct("8901234567890", "Router WiFi", "Router de doble banda con alta cobertura", new BigDecimal("59.99"), 12);
        createProduct("9012345678901", "Cámara Web HD", "Cámara web con micrófono integrado", new BigDecimal("39.99"), 18);
        createProduct("0123456789012", "Altavoces Bluetooth", "Altavoces inalámbricos con excelente calidad de sonido", new BigDecimal("69.99"), 15);
    }

    /**
     * Creates a product.
     *
     * @param barcode The barcode
     * @param name The product name
     * @param description The product description
     * @param price The product price
     * @param stock The initial stock
     * @return The created product
     */
    private Product createProduct(String barcode, String name, String description, BigDecimal price, Integer stock) {
        Product product = new Product(barcode, name, description, price, stock);
        return productRepository.save(product);
    }
}