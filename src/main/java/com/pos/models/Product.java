package com.pos.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Entity representing a product in the inventory.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El código de barras es obligatorio")
    private String barcode;

    @Column(nullable = false)
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Debe inidicar si esta activo o no el producto")
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Price price;

    @Column
    @NotNull(message = "Debe indicar la fecha de creacion del producto")
    private Timestamp creationDate;


    /**
     * Default constructor.
     */
    public Product() {
        this.creationDate = new Timestamp(System.currentTimeMillis());
    }

    
    public Product(String barcode,
                   String name,
                   String description,
                   Price price,
                   boolean active,
                   Category category) {
        this.barcode = barcode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
        this.category = category;
        this.creationDate = new Timestamp(System.currentTimeMillis());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", barcode='" + barcode + '\'' +
                ", name='" + name + '\'' +
                ", isActive='" + active + '\'' +
                '}';
    }
}