package com.pos.service;

import com.pos.models.Product;
import com.pos.models.Sale;
import com.pos.models.SaleItem;
import com.pos.models.User;
import com.pos.repositories.ProductRepository;
import com.pos.repositories.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class SaleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaleService.class);

    // Tasa de impuesto (ejemplo: 16%)
    private static final BigDecimal TAX_RATE = new BigDecimal("0.21");

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    // Constructor con inyección de dependencias en lugar de @RequiredArgsConstructor
    public SaleService(SaleRepository saleRepository, ProductRepository productRepository, UserService userService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    /**
     * Obtiene una venta por su ID
     */
    public Sale getSaleById(Long saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + saleId));
    }

    /**
     * Crea una nueva venta con estado IN_PROGRESS
     */
    @Transactional
    public Sale createNewSale() {
        // Obtener el usuario actual
        User currentUser = userService.getCurrentUser();

        Sale sale = new Sale();
        sale.setCashier(currentUser);
        sale.setStatus(Sale.SaleStatus.IN_PROGRESS);
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setTaxAmount(BigDecimal.ZERO);
        sale.setTotalAmount(BigDecimal.ZERO);

        // Generar número de factura único basado en fecha y secuencia
        String invoiceNumber = generateInvoiceNumber();
        sale.setInvoiceNumber(invoiceNumber);

        return saleRepository.save(sale);
    }

    /**
     * Añade un producto a una venta existente utilizando el código de barras
     */
    @Transactional
    public SaleItem addProductByBarcode(Long saleId, String barcode, int quantity) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + saleId));

        if (sale.getStatus() != Sale.SaleStatus.IN_PROGRESS) {
            throw new IllegalStateException("No se puede modificar una venta que no está en progreso");
        }

        // Buscar el producto por código de barras
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con código de barras: " + barcode));

        // Verificar stock
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + product.getName());
        }

        // Verificar si el producto ya está en la venta
        Optional<SaleItem> existingItem = sale.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        SaleItem saleItem;

        if (existingItem.isPresent()) {
            // Actualizar cantidad del ítem existente
            saleItem = existingItem.get();
            int newQuantity = saleItem.getQuantity() + quantity;

            if (product.getStock() < newQuantity) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + product.getName());
            }

            saleItem.setQuantity(newQuantity);
            saleItem.calculateTotals();
        } else {
            // Crear nuevo ítem
            saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(quantity);
            saleItem.setUnitPrice(product.getPrice());
            saleItem.setDiscount(BigDecimal.ZERO);
            saleItem.calculateTotals();

            sale.getItems().add(saleItem);
        }

        // Actualizar totales de la venta
        updateSaleTotals(sale);

        // Guardar la venta actualizada
        saleRepository.save(sale);

        return saleItem;
    }

    /**
     * Elimina un ítem de la venta
     */
    @Transactional
    public void removeItem(Long saleId, Long itemId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + saleId));

        if (sale.getStatus() != Sale.SaleStatus.IN_PROGRESS) {
            throw new IllegalStateException("No se puede modificar una venta que no está en progreso");
        }

        // Buscar el ítem a eliminar
        boolean removed = sale.getItems().removeIf(item -> item.getId().equals(itemId));

        if (!removed) {
            throw new IllegalArgumentException("Ítem no encontrado en la venta: " + itemId);
        }

        // Actualizar totales
        updateSaleTotals(sale);

        // Guardar la venta actualizada
        saleRepository.save(sale);
    }

    /**
     * Completa una venta, actualizando el stock y cambiando su estado
     */
    @Transactional
    public Sale completeSale(Long saleId, Sale.PaymentMethod paymentMethod, String customerName, String customerTaxId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + saleId));

        if (sale.getStatus() != Sale.SaleStatus.IN_PROGRESS) {
            throw new IllegalStateException("La venta ya ha sido completada o cancelada");
        }

        if (sale.getItems().isEmpty()) {
            throw new IllegalStateException("No se puede completar una venta sin productos");
        }

        // Actualizar detalles de la venta
        sale.setPaymentMethod(paymentMethod);
        sale.setCustomerName(customerName);
        sale.setCustomerTaxId(customerTaxId);
        sale.setStatus(Sale.SaleStatus.COMPLETED);

        // Actualizar stock de productos
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getStock() - item.getQuantity();

            if (newStock < 0) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + product.getName());
            }

            product.setStock(newStock);
            productRepository.save(product);
        }

        return saleRepository.save(sale);
    }

    /**
     * Cancela una venta en progreso
     */
    @Transactional
    public Sale cancelSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + saleId));

        if (sale.getStatus() != Sale.SaleStatus.IN_PROGRESS) {
            throw new IllegalStateException("Solo se pueden cancelar ventas en progreso");
        }

        sale.setStatus(Sale.SaleStatus.CANCELLED);
        return saleRepository.save(sale);
    }

    /**
     * Actualiza los totales de una venta basado en sus ítems
     */
    private void updateSaleTotals(Sale sale) {
        BigDecimal subtotal = sale.getItems().stream()
                .map(SaleItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular impuesto
        BigDecimal taxAmount = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

        // Calcular total
        BigDecimal totalAmount = subtotal.add(taxAmount);

        sale.setSubtotal(subtotal);
        sale.setTaxAmount(taxAmount);
        sale.setTotalAmount(totalAmount);
    }

    /**
     * Genera un número de factura único basado en fecha y secuencia
     */
    private String generateInvoiceNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePrefix = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Obtener el último número de factura para generar secuencia
        String prefix = datePrefix + "-";
        List<Sale> todaySales = saleRepository.findAll().stream()
                .filter(s -> s.getInvoiceNumber().startsWith(prefix))
                .toList();

        int sequence = todaySales.size() + 1;

        return String.format("%s-%04d", datePrefix, sequence);
    }

    /**
     * Obtiene las ventas del día actual
     */
    public List<Sale> getTodaySales() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return saleRepository.findBySaleDateBetween(startOfDay, endOfDay);
    }

    /**
     * Obtiene las ventas entre dos fechas
     */
    public List<Sale> getSalesBetweenDates(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end);
    }

    /**
     * Obtiene el total de ventas del día
     */
    public BigDecimal getTodayTotalSales() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        BigDecimal total = saleRepository.getTotalSalesBetween(startOfDay, endOfDay);
        return total != null ? total : BigDecimal.ZERO;
    }
}