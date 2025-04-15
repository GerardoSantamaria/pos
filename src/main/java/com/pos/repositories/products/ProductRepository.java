package com.pos.repositories.products;

import com.pos.models.products.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for accessing and manipulating Product entities.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds a product by its barcode.
     *
     * @param barcode The barcode to search for
     * @return An Optional containing the product if found
     */
    Optional<Product> findByBarcode(String barcode);

    /**
     * Checks if a product with the given barcode exists.
     *
     * @param barcode The barcode to check
     * @return True if a product with the barcode exists, false otherwise
     */
    boolean existsByBarcode(String barcode);

    /**
     * Searches for products that match the given search term in name or barcode.
     *
     * @param searchTerm The search term
     * @param pageable The pagination information
     * @return A Page of Product entities
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchTerm% OR p.barcode LIKE %:searchTerm%")
    Page<Product> searchByNameOrBarcode(@Param("searchTerm") String searchTerm, Pageable pageable);

    //Page<Product> findByStockLessThanEqual(Integer threshold, Pageable pageable);
}