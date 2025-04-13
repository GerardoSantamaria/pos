package com.pos.config;

import com.pos.models.*;
import com.pos.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final TaxRepository taxRepository;
    private final PriceRepository priceRepository;
    private final CategoryRepository categoryRepository;

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
            PasswordEncoder passwordEncoder, TaxRepository taxRepository, PriceRepository priceRepository, CategoryRepository categoryRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.taxRepository = taxRepository;
        this.priceRepository = priceRepository;
        this.categoryRepository = categoryRepository;
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
        Tax tax = new Tax();
        tax.setName("IVA");
        tax.setPercentage(new BigDecimal("21"));
        tax = taxRepository.save(tax);


        Category c1 = new Category("Electronica 1", null, true);
        Category c2 = new Category("Electronica 2", null, true);
        c1 = categoryRepository.save(c1);
        c2 = categoryRepository.save(c2);


        // Create sample products
        createProduct("1234567890123", "Laptop HP ProBook", "Laptop de alta gama para uso profesional", new BigDecimal("999.99"), new BigDecimal("600"),10, tax, c1);
        createProduct("2345678901234", "Monitor LG 24\"", "Monitor LED Full HD", new BigDecimal("199.99"),  new BigDecimal("70"),15, tax, c1);
        createProduct("3456789012345", "Teclado Mecánico Logitech", "Teclado mecánico con retroiluminación RGB", new BigDecimal("89.99"), new BigDecimal("35"),20, tax, c1);
        createProduct("4567890123456", "Mouse Inalámbrico", "Mouse ergonómico inalámbrico", new BigDecimal("29.99"), new BigDecimal("10"),30, tax, c1);
        createProduct("5678901234567", "Disco Duro SSD 1TB", "Disco de estado sólido de alta velocidad", new BigDecimal("149.99"), new BigDecimal("85.50"), 25, tax, c1);
        createProduct("6789012345678", "Memoria RAM 16GB", "Memoria RAM DDR4 de alta velocidad", new BigDecimal("79.99"), new BigDecimal("29.99"), 40, tax, c2);
        createProduct("7890123456789", "Impresora HP LaserJet", "Impresora láser monocromática", new BigDecimal("249.99"), new BigDecimal("100.99"), 8, tax, c2);
        createProduct("8901234567890", "Router WiFi", "Router de doble banda con alta cobertura", new BigDecimal("59.99"), new BigDecimal("29.99"), 12, tax, c2);
        createProduct("9012345678901", "Cámara Web HD", "Cámara web con micrófono integrado", new BigDecimal("39.99"),new BigDecimal("23"), 18, tax, c2);
        createProduct("0123456789012", "Altavoces Bluetooth", "Altavoces inalámbricos con excelente calidad de sonido", new BigDecimal("69.99"), new BigDecimal("29.99"), 15, tax, c2);
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
    private Product createProduct(String barcode, String name, String description, BigDecimal price,BigDecimal cost, Integer stock, Tax tax, Category category) {
        Product product = new Product(barcode, name, description, this.createPrice(price, tax), true, category);
        return productRepository.save(product);
    }

    private Price createPrice(BigDecimal value, Tax tax) {
        Price price = new Price();
        price.setSalePrice(value);
        price.setPurchasePrice(value.subtract(new BigDecimal("5")));
        price.setUpdateDate(LocalDateTime.now());
        price.setTax(tax);
        return priceRepository.save(price);
    }
}