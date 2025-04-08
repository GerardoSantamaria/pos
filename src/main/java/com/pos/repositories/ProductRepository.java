package com.pos.repositories;

import com.pos.models.Category;
import com.pos.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar producto por código de barras (para el escáner)
    Optional<Product> findByBarcode(String barcode);

    // Buscar por SKU
    Optional<Product> findBySku(String sku);

    // Buscar productos por nombre (parcial)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Buscar productos por categoría
    List<Product> findByCategory(Category category);

    // Productos con stock bajo
    @Query("SELECT p FROM Product p WHERE p.stock <= :threshold AND p.active = true")
    List<Product> findLowStockProducts(int threshold);

    // Productos activos
    List<Product> findByActiveTrue();
}