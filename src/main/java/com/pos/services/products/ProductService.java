package com.pos.services.products;

import com.pos.models.products.Product;
import com.pos.repositories.products.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Product entities.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Constructor with dependencies.
     *
     * @param productRepository The product repository
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Creates a new product.
     *
     * @param product The product to create
     * @return The created product
     * @throws IllegalArgumentException If a product with the same barcode already exists
     */
    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.existsByBarcode(product.getBarcode())) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + product.getBarcode());
        }
        return productRepository.save(product);
    }

    /**
     * Updates an existing product.
     *
     * @param product The product to update
     * @return The updated product
     * @throws IllegalArgumentException If the product is not found
     */
    @Transactional
    public Product updateProduct(Product product) {
        // Get existing product
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + product.getId()));

        // Check if barcode is being changed and if the new barcode already exists
        if (!existingProduct.getBarcode().equals(product.getBarcode()) &&
                productRepository.existsByBarcode(product.getBarcode())) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + product.getBarcode());
        }

        // Update fields
        existingProduct.setBarcode(product.getBarcode());
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        //existingProduct.setStock(product.getStock());

        // Save updated product
        return productRepository.save(existingProduct);
    }

    /**
     * Finds a product by its ID.
     *
     * @param id The product ID
     * @return An Optional containing the product if found
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Finds a product by its barcode.
     *
     * @param barcode The barcode
     * @return An Optional containing the product if found
     */
    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    /**
     * Gets all products.
     *
     * @return List of all products
     */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * Gets a page of products.
     *
     * @param page The page number (zero-based)
     * @param size The page size
     * @param sortBy The field to sort by
     * @param ascending Whether to sort in ascending order
     * @return A Page of products
     */
    public Page<Product> findAllPaginated(int page, int size, String sortBy, boolean ascending) {
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
    }

    /**
     * Searches for products by name or barcode.
     *
     * @param searchTerm The search term
     * @param page The page number (zero-based)
     * @param size The page size
     * @return A Page of products
     */
    public Page<Product> searchProducts(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchByNameOrBarcode(searchTerm, pageable);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id The product ID
     */
    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Updates the stock of a product.
     *
     * @param id The product ID
     * @param quantity The quantity to add (positive) or remove (negative)
     * @return The updated product
     * @throws IllegalArgumentException If the product is not found or if removing would result in negative stock
     */
    @Transactional
    public Product updateStock(Long id, int quantity) {
        // Get existing product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        /*
        // Calculate new stock
        int newStock = product.getStock() + quantity;

        // Check if new stock would be negative
        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo. Stock actual: " +
                    product.getStock() + ", Cantidad a restar: " + Math.abs(quantity));
        }

        // Update stock
        product.setStock(newStock);

         */

        // Save updated product
        return productRepository.save(product);
    }

}