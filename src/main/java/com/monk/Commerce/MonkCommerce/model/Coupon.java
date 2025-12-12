package com.monk.Commerce.MonkCommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(nullable = false)
    private Double discountValue;

    // For CART_WISE: minimum cart total required
    private Double minimumCartTotal;

    // For PRODUCT_WISE: list of product IDs this coupon applies to
    @ElementCollection
    @CollectionTable(name = "coupon_applicable_products", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    private List<Long> applicableProductIds;

    // For BXGY: buy X quantity
    private Integer buyQuantity;

    // For BXGY: get Y quantity free
    private Integer getQuantity;

    // For BXGY: products to buy (can be multiple)
    @ElementCollection
    @CollectionTable(name = "coupon_buy_products", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    private List<Long> buyProductIds;

    // For BXGY: products to get free (can be multiple)
    @ElementCollection
    @CollectionTable(name = "coupon_free_products", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    private List<Long> freeProductIds;

    // For BXGY: maximum number of times this deal can be applied
    private Integer maxRepetitions;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Expiration dates
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

