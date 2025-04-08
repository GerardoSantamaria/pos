package com.pos.services;

import com.pos.models.Product;
import com.pos.repositories.CategoryRepository;
import com.pos.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Busca un producto por código de barras
     */
    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    /**
     * Obtiene todos los productos activos
     */
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    /**
     * Guarda un producto nuevo o actualiza uno existente
     */
    @Transactional
    public Product saveProduct(Product product) {
        // Validar que el código de barras y SKU sean únicos si es un nuevo producto
        if (product.getId() == null) {
            validateUniqueBarcode(product.getBarcode());
            validateUniqueSku(product.getSku());
        } else {
            // Si es una actualización, verificar que no existe otro producto con el mismo barcode/sku
            Optional<Product> existingWithBarcode = productRepository.findByBarcode(product.getBarcode());
            if (existingWithBarcode.isPresent() && !existingWithBarcode.get().getId().equals(product.getId())) {
                throw new IllegalArgumentException("Ya existe otro producto con el código de barras: " + product.getBarcode());
            }

            Optional<Product> existingWithSku = productRepository.findBySku(product.getSku());
            if (existingWithSku.isPresent() && !existingWithSku.get().getId().equals(product.getId())) {
                throw new IllegalArgumentException("Ya existe otro producto con el SKU: " + product.getSku());
            }
        }

        return productRepository.save(product);
    }

    /**
     * Busca productos por nombre (búsqueda parcial)
     */
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Obtiene productos con stock bajo
     */
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    /**
     * Desactiva un producto (eliminación suave)
     */
    @Transactional
    public void deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        product.setActive(false);
        productRepository.save(product);
    }

    /**
     * Actualiza el stock de un producto
     */
    @Transactional
    public Product updateStock(Long productId, int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        product.setStock(newStock);
        return productRepository.save(product);
    }

    /**
     * Valida que el código de barras sea único
     */
    private void validateUniqueBarcode(String barcode) {
        if (productRepository.findByBarcode(barcode).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + barcode);
        }
    }

    /**
     * Valida que el SKU sea único
     */
    private void validateUniqueSku(String sku) {
        if (productRepository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el SKU: " + sku);
        }
    }
}