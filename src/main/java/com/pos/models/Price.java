package com.pos.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Price")
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "sale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "wholesale_price", precision = 12, scale = 2)
    private BigDecimal wholesalePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_id")
    private Tax tax;

    @Column(name = "update_date", updatable = false)
    private LocalDateTime updateDate = LocalDateTime.now();

    // Constructores
    public Price() {}

    public Price(Product product, BigDecimal purchasePrice, BigDecimal salePrice,
                 BigDecimal wholesalePrice, Tax tax) {
        this.product = product;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.wholesalePrice = wholesalePrice;
        this.tax = tax;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public BigDecimal getWholesalePrice() { return wholesalePrice; }
    public void setWholesalePrice(BigDecimal wholesalePrice) { this.wholesalePrice = wholesalePrice; }

    public Tax getTax() { return tax; }
    public void setTax(Tax tax) { this.tax = tax; }

    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }

    // Métodos útiles
    public BigDecimal getFinalPrice(boolean isWholesale) {
        BigDecimal basePrice = isWholesale && wholesalePrice != null ?
                wholesalePrice : salePrice;

        if(tax != null) {
            return basePrice.multiply(BigDecimal.ONE.add(
                    tax.getPercentage().divide(BigDecimal.valueOf(100))));
        }
        return basePrice;
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", salePrice=" + salePrice +
                ", updateDate=" + updateDate +
                '}';
    }
}