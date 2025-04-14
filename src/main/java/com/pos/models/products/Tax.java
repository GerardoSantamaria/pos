package com.pos.models.products;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "taxes")
public class Tax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull(message = "El nombre es obligatorio")
    private String name;

    @Column(length = 1000)
    private String description;

    @Column
    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor debe ser mayor que cero")
    private BigDecimal percentage;

    @OneToMany(mappedBy = "tax", fetch = FetchType.LAZY)
    private List<Price> prices = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
