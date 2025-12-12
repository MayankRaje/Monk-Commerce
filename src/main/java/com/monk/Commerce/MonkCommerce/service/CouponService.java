package com.monk.Commerce.MonkCommerce.service;

import com.monk.Commerce.MonkCommerce.dto.*;
import com.monk.Commerce.MonkCommerce.exception.*;
import com.monk.Commerce.MonkCommerce.model.*;
import com.monk.Commerce.MonkCommerce.repository.CartRepository;
import com.monk.Commerce.MonkCommerce.repository.CouponRepository;
import com.monk.Commerce.MonkCommerce.repository.ProductRepository;
import com.monk.Commerce.MonkCommerce.service.strategy.CouponDiscountStrategy;
import com.monk.Commerce.MonkCommerce.service.strategy.CouponStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CouponStrategyFactory strategyFactory;

    public CouponResponseDTO createCoupon(CouponRequestDTO request) {
        validateCouponRequest(request);
        Coupon coupon = mapToEntity(request);
        coupon = couponRepository.save(coupon);
        return mapToResponseDTO(coupon);
    }

    public List<CouponResponseDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public CouponResponseDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with id: " + id));
        return mapToResponseDTO(coupon);
    }

    public CouponResponseDTO updateCoupon(Long id, CouponRequestDTO request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with id: " + id));
        validateCouponRequest(request);
        updateCouponFromDTO(coupon, request);
        coupon = couponRepository.save(coupon);
        return mapToResponseDTO(coupon);
    }

    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new CouponNotFoundException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
    }

    public List<ApplicableCouponDTO> getApplicableCoupons(CartRequestDTO cartRequest) {
        Cart cart = buildCartFromRequest(cartRequest);
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> allCoupons = couponRepository.findAll().stream()
                .filter(Coupon::getIsActive)
                .filter(coupon -> isCouponValid(coupon, now))
                .collect(Collectors.toList());

        List<ApplicableCouponDTO> applicableCoupons = new ArrayList<>();

        for (Coupon coupon : allCoupons) {
            CouponDiscountStrategy strategy = strategyFactory.getStrategy(coupon.getType());
            ApplicableCouponDTO applicableCoupon = strategy.checkApplicability(coupon, cart);
            if (applicableCoupon != null) {
                applicableCoupons.add(applicableCoupon);
            }
        }

        return applicableCoupons;
    }

    public CouponResponseDTO createCouponFromFormat(CouponRequestFormatDTO request) {
        CouponRequestDTO internalRequest = convertFromFormat(request);
        return createCoupon(internalRequest);
    }

    public ApplicableCouponsResponseDTO getApplicableCouponsFromFormat(CartRequestFormatDTO cartRequest) {
        CartRequestDTO internalRequest = convertCartFromFormat(cartRequest);
        List<ApplicableCouponDTO> applicableCoupons = getApplicableCoupons(internalRequest);
        
        List<ApplicableCouponFormatDTO> formatCoupons = applicableCoupons.stream()
                .map(this::convertToFormat)
                .collect(Collectors.toList());
        
        ApplicableCouponsResponseDTO response = new ApplicableCouponsResponseDTO();
        response.setApplicable_coupons(formatCoupons);
        return response;
    }

    public ApplyCouponResponseDTO applyCouponFromFormat(Long couponId, CartRequestFormatDTO cartRequest) {
        CartRequestDTO internalRequest = convertCartFromFormat(cartRequest);
        CartResponseDTO cartResponse = applyCoupon(couponId, internalRequest);
        return convertToApplyCouponResponse(cartResponse);
    }

    @Transactional
    public CartResponseDTO applyCoupon(Long couponId, CartRequestDTO cartRequest) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with id: " + couponId));

        if (!coupon.getIsActive()) {
            throw new InvalidCouponException("Coupon is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!isCouponValid(coupon, now)) {
            throw new InvalidCouponException("Coupon is not valid at this time");
        }

        Cart cart = buildCartFromRequest(cartRequest);
        cart.setAppliedCoupon(coupon);

        CouponDiscountStrategy strategy = strategyFactory.getStrategy(coupon.getType());
        ApplicableCouponDTO applicableCoupon = strategy.checkApplicability(coupon, cart);
        if (applicableCoupon == null) {
            throw new CouponNotApplicableException("Coupon is not applicable to this cart");
        }

        strategy.applyDiscount(coupon, cart);
        double totalDiscount = cart.getItems().stream()
                .mapToDouble(item -> item.getDiscountAmount() != null ? item.getDiscountAmount() : 0.0)
                .sum();
        cart.setTotalDiscount(totalDiscount);
        cart.setFinalAmount(cart.getTotalAmount() - totalDiscount);
        cart = cartRepository.save(cart);

        return mapToCartResponseDTO(cart);
    }

    private boolean isCouponValid(Coupon coupon, LocalDateTime now) {
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            return false;
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            return false;
        }
        return true;
    }



    private Cart buildCartFromRequest(CartRequestDTO cartRequest) {
        Cart cart = new Cart();
        List<CartItem> items = new ArrayList<>();
        double totalAmount = 0.0;

        for (CartItemDTO itemDTO : cartRequest.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + itemDTO.getProductId()));

            double itemPrice = (itemDTO.getPrice() != null) ? itemDTO.getPrice() : product.getPrice();

            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(itemDTO.getQuantity());
            cartItem.setOriginalPrice(itemPrice);
            cartItem.setCart(cart);

            items.add(cartItem);
            totalAmount += itemPrice * itemDTO.getQuantity();
        }

        cart.setItems(items);
        cart.setTotalAmount(totalAmount);
        return cart;
    }

    private void validateCouponRequest(CouponRequestDTO request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidCouponException("Coupon name is required");
        }
        if (request.getType() == null) {
            throw new InvalidCouponException("Coupon type is required");
        }
        if (request.getDiscountType() == null) {
            throw new InvalidCouponException("Discount type is required");
        }
        if (request.getDiscountValue() == null || request.getDiscountValue() <= 0) {
            throw new InvalidCouponException("Discount value must be greater than 0");
        }

        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new InvalidCouponException("Start date must be before end date");
            }
        }

        if (request.getType() == CouponType.CART_WISE) {
            if (request.getMinimumCartTotal() == null || request.getMinimumCartTotal() <= 0) {
                throw new InvalidCouponException("Minimum cart total is required for cart-wise coupons");
            }
        } else if (request.getType() == CouponType.PRODUCT_WISE) {
            if (request.getApplicableProductIds() == null || request.getApplicableProductIds().isEmpty()) {
                throw new InvalidCouponException("Applicable product IDs are required for product-wise coupons");
            }
        } else if (request.getType() == CouponType.BXGY) {
            if (request.getBuyQuantity() == null || request.getBuyQuantity() <= 0) {
                throw new InvalidCouponException("Buy quantity is required for BxGy coupons");
            }
            if (request.getGetQuantity() == null || request.getGetQuantity() <= 0) {
                throw new InvalidCouponException("Get quantity is required for BxGy coupons");
            }
            if (request.getBuyProductIds() == null || request.getBuyProductIds().isEmpty()) {
                throw new InvalidCouponException("Buy product IDs are required for BxGy coupons");
            }
            if (request.getFreeProductIds() == null || request.getFreeProductIds().isEmpty()) {
                throw new InvalidCouponException("Free product IDs are required for BxGy coupons");
            }
        }
    }

    private Coupon mapToEntity(CouponRequestDTO dto) {
        Coupon coupon = new Coupon();
        coupon.setName(dto.getName());
        coupon.setType(dto.getType());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinimumCartTotal(dto.getMinimumCartTotal());
        coupon.setApplicableProductIds(dto.getApplicableProductIds());
        coupon.setBuyQuantity(dto.getBuyQuantity());
        coupon.setGetQuantity(dto.getGetQuantity());
        coupon.setBuyProductIds(dto.getBuyProductIds());
        coupon.setFreeProductIds(dto.getFreeProductIds());
        coupon.setMaxRepetitions(dto.getMaxRepetitions());
        coupon.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        coupon.setStartDate(dto.getStartDate());
        coupon.setEndDate(dto.getEndDate());
        return coupon;
    }

    private void updateCouponFromDTO(Coupon coupon, CouponRequestDTO dto) {
        coupon.setName(dto.getName());
        coupon.setType(dto.getType());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinimumCartTotal(dto.getMinimumCartTotal());
        coupon.setApplicableProductIds(dto.getApplicableProductIds());
        coupon.setBuyQuantity(dto.getBuyQuantity());
        coupon.setGetQuantity(dto.getGetQuantity());
        coupon.setBuyProductIds(dto.getBuyProductIds());
        coupon.setFreeProductIds(dto.getFreeProductIds());
        coupon.setMaxRepetitions(dto.getMaxRepetitions());
        if (dto.getIsActive() != null) {
            coupon.setIsActive(dto.getIsActive());
        }
        coupon.setStartDate(dto.getStartDate());
        coupon.setEndDate(dto.getEndDate());
    }

    private CouponResponseDTO mapToResponseDTO(Coupon coupon) {
        CouponResponseDTO dto = new CouponResponseDTO();
        dto.setId(coupon.getId());
        dto.setName(coupon.getName());
        dto.setType(coupon.getType());
        dto.setDiscountType(coupon.getDiscountType());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setMinimumCartTotal(coupon.getMinimumCartTotal());
        dto.setApplicableProductIds(coupon.getApplicableProductIds());
        dto.setBuyQuantity(coupon.getBuyQuantity());
        dto.setGetQuantity(coupon.getGetQuantity());
        dto.setBuyProductIds(coupon.getBuyProductIds());
        dto.setFreeProductIds(coupon.getFreeProductIds());
        dto.setMaxRepetitions(coupon.getMaxRepetitions());
        dto.setIsActive(coupon.getIsActive());
        dto.setStartDate(coupon.getStartDate());
        dto.setEndDate(coupon.getEndDate());
        return dto;
    }

    private CartResponseDTO mapToCartResponseDTO(Cart cart) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setTotalDiscount(cart.getTotalDiscount());
        dto.setFinalAmount(cart.getFinalAmount());
        if (cart.getAppliedCoupon() != null) {
            dto.setAppliedCouponId(cart.getAppliedCoupon().getId());
            dto.setAppliedCouponName(cart.getAppliedCoupon().getName());
        }

        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .map(item -> {
                    CartItemResponseDTO itemDTO = new CartItemResponseDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setOriginalPrice(item.getOriginalPrice());
                    itemDTO.setDiscountedPrice(item.getDiscountedPrice());
                    itemDTO.setDiscountAmount(item.getDiscountAmount());
                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        return dto;
    }

    private CouponRequestDTO convertFromFormat(CouponRequestFormatDTO formatRequest) {
        CouponRequestDTO request = new CouponRequestDTO();
        CouponDetailsDTO details = formatRequest.getDetails();
        
        String typeStr = formatRequest.getType().toLowerCase();
        CouponType type;
        if (typeStr.equals("cart-wise")) {
            type = CouponType.CART_WISE;
        } else if (typeStr.equals("product-wise")) {
            type = CouponType.PRODUCT_WISE;
        } else if (typeStr.equals("bxgy")) {
            type = CouponType.BXGY;
        } else {
            throw new InvalidCouponException("Invalid coupon type: " + formatRequest.getType());
        }
        
        // Generate a default name if not provided
        request.setName("Coupon-" + typeStr + "-" + System.currentTimeMillis());
        request.setType(type);
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setIsActive(true);
        
        if (type == CouponType.CART_WISE) {
            request.setMinimumCartTotal(details.getThreshold());
            request.setDiscountValue(details.getDiscount());
        } else if (type == CouponType.PRODUCT_WISE) {
            request.setApplicableProductIds(List.of(details.getProduct_id()));
            request.setDiscountValue(details.getDiscount());
        } else if (type == CouponType.BXGY) {
            if (details.getBuy_products() != null && !details.getBuy_products().isEmpty()) {
                int buyQty = details.getBuy_products().stream()
                        .mapToInt(BuyProductDTO::getQuantity)
                        .min()
                        .orElse(1);
                request.setBuyQuantity(buyQty);
                request.setBuyProductIds(details.getBuy_products().stream()
                        .map(BuyProductDTO::getProduct_id)
                        .collect(Collectors.toList()));
            }
            
            if (details.getGet_products() != null && !details.getGet_products().isEmpty()) {
                int getQty = details.getGet_products().get(0).getQuantity();
                if (getQty <= 0) getQty = 1;
                request.setGetQuantity(getQty);
                request.setFreeProductIds(details.getGet_products().stream()
                        .map(GetProductDTO::getProduct_id)
                        .collect(Collectors.toList()));
            }
            
            request.setMaxRepetitions(details.getRepition_limit());
        }
        
        request.setStartDate(details.getStart_date());
        request.setEndDate(details.getEnd_date());
        
        return request;
    }

    private CartRequestDTO convertCartFromFormat(CartRequestFormatDTO formatRequest) {
        CartRequestDTO request = new CartRequestDTO();
        List<CartItemDTO> items = formatRequest.getCart().getItems().stream()
                .map(item -> {
                    CartItemDTO dto = new CartItemDTO();
                    dto.setProductId(item.getProduct_id());
                    dto.setQuantity(item.getQuantity());
                    dto.setPrice(item.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
        request.setItems(items);
        return request;
    }

    private ApplicableCouponFormatDTO convertToFormat(ApplicableCouponDTO dto) {
        ApplicableCouponFormatDTO format = new ApplicableCouponFormatDTO();
        format.setCoupon_id(dto.getId());
        
        String typeStr;
        switch (dto.getType()) {
            case CART_WISE:
                typeStr = "cart-wise";
                break;
            case PRODUCT_WISE:
                typeStr = "product-wise";
                break;
            case BXGY:
                typeStr = "bxgy";
                break;
            default:
                typeStr = dto.getType().toString().toLowerCase();
        }
        format.setType(typeStr);
        format.setDiscount(dto.getTotalDiscount());
        return format;
    }

    private ApplyCouponResponseDTO convertToApplyCouponResponse(CartResponseDTO cartResponse) {
        ApplyCouponResponseDTO response = new ApplyCouponResponseDTO();
        UpdatedCartDTO updatedCart = new UpdatedCartDTO();
        
        List<CartItemResponseFormatDTO> items = cartResponse.getItems().stream()
                .map(item -> {
                    CartItemResponseFormatDTO formatItem = new CartItemResponseFormatDTO();
                    formatItem.setProduct_id(item.getProductId());
                    formatItem.setQuantity(item.getQuantity());
                    formatItem.setPrice(item.getOriginalPrice());
                    formatItem.setTotal_discount(item.getDiscountAmount() != null ? item.getDiscountAmount() : 0.0);
                    return formatItem;
                })
                .collect(Collectors.toList());
        
        updatedCart.setItems(items);
        updatedCart.setTotal_price(cartResponse.getTotalAmount());
        updatedCart.setTotal_discount(cartResponse.getTotalDiscount());
        updatedCart.setFinal_price(cartResponse.getFinalAmount());
        
        response.setUpdated_cart(updatedCart);
        return response;
    }
}

