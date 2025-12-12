package com.monk.Commerce.MonkCommerce.service;

import com.monk.Commerce.MonkCommerce.dto.*;
import com.monk.Commerce.MonkCommerce.exception.*;
import com.monk.Commerce.MonkCommerce.model.*;
import com.monk.Commerce.MonkCommerce.repository.CartRepository;
import com.monk.Commerce.MonkCommerce.repository.CouponRepository;
import com.monk.Commerce.MonkCommerce.repository.ProductRepository;
import com.monk.Commerce.MonkCommerce.service.strategy.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CombinedTest {


    //COMMON MOCKS


    @Mock private CouponRepository couponRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CouponStrategyFactory strategyFactory;
    @Mock private CouponDiscountStrategy mockStrategy;

    @InjectMocks
    private CouponService couponService;

    // extra mocks only for factory tests
    @Mock private ProductWiseStrategy productWiseStrategy;
    @Mock private CartWiseStrategy cartWiseStrategy;
    @Mock private BxGyStrategy bxGyStrategy;

    //COUPON SERVICE TESTS


    @Test
    void testApplyCoupon_successful() {

        Long couponId = 1L;

        // Coupon in DB
        Coupon coupon = new Coupon();
        coupon.setId(couponId);
        coupon.setName("SAVE10");
        coupon.setType(CouponType.CART_WISE);
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(10.0);
        coupon.setMinimumCartTotal(0.0);
        coupon.setIsActive(true);
        coupon.setStartDate(LocalDateTime.now().minusDays(1));
        coupon.setEndDate(LocalDateTime.now().plusDays(5));

        // request cart
        CartItemDTO itemDTO = new CartItemDTO();
        itemDTO.setProductId(100L);
        itemDTO.setQuantity(2);
        itemDTO.setPrice(100.0);

        CartRequestDTO cartRequestDTO = new CartRequestDTO();
        cartRequestDTO.setItems(List.of(itemDTO));

        // product used in buildCartFromRequest
        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        when(productRepository.findById(100L))
                .thenReturn(Optional.of(product));

        when(couponRepository.findById(couponId))
                .thenReturn(Optional.of(coupon));
        when(strategyFactory.getStrategy(CouponType.CART_WISE))
                .thenReturn(mockStrategy);

        // make coupon applicable
        ApplicableCouponDTO applicable = new ApplicableCouponDTO();
        applicable.setId(couponId);
        applicable.setName("SAVE10");
        applicable.setType(CouponType.CART_WISE);
        applicable.setTotalDiscount(20.0);
        applicable.setReason("ok");
        when(mockStrategy.checkApplicability(any(Coupon.class), any(Cart.class)))
                .thenReturn(applicable);

        // simulate discount distribution
        doAnswer(invocation -> {
            Coupon c = invocation.getArgument(0);
            Cart cart = invocation.getArgument(1);
            double totalAmount = cart.getTotalAmount(); // 200
            double discount = totalAmount * (c.getDiscountValue() / 100.0); // 20
            CartItem cartItem = cart.getItems().get(0);
            cartItem.setDiscountAmount(discount);
            cartItem.setDiscountedPrice(totalAmount - discount);
            return null;
        }).when(mockStrategy).applyDiscount(any(Coupon.class), any(Cart.class));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CartResponseDTO response = couponService.applyCoupon(couponId, cartRequestDTO);

        assertNotNull(response);
        assertEquals(couponId, response.getAppliedCouponId());
        assertEquals("SAVE10", response.getAppliedCouponName());
        assertEquals(200.0, response.getTotalAmount(), 0.001);
        assertEquals(20.0, response.getTotalDiscount(), 0.001);
        assertEquals(180.0, response.getFinalAmount(), 0.001);
    }



    @Test
    void testApplyCoupon_couponNotFound() {

        Long couponId = 99L;
        when(couponRepository.findById(couponId))
                .thenReturn(Optional.empty());

        CartRequestDTO cartRequestDTO = new CartRequestDTO();
        cartRequestDTO.setItems(List.of());

        assertThrows(CouponNotFoundException.class,
                () -> couponService.applyCoupon(couponId, cartRequestDTO));
    }

    @Test
    void testApplyCoupon_inactiveCoupon() {

        Long couponId = 1L;
        Coupon coupon = new Coupon();
        coupon.setId(couponId);
        coupon.setIsActive(false);

        when(couponRepository.findById(couponId))
                .thenReturn(Optional.of(coupon));

        CartRequestDTO cartRequestDTO = new CartRequestDTO();
        cartRequestDTO.setItems(List.of());

        assertThrows(InvalidCouponException.class,
                () -> couponService.applyCoupon(couponId, cartRequestDTO));
    }

    @Test
    void testApplyCoupon_invalidTimeWindow() {

        Long couponId = 1L;
        Coupon coupon = new Coupon();
        coupon.setId(couponId);
        coupon.setIsActive(true);
        coupon.setStartDate(LocalDateTime.now().plusDays(1)); // not started
        coupon.setEndDate(LocalDateTime.now().plusDays(5));

        when(couponRepository.findById(couponId))
                .thenReturn(Optional.of(coupon));

        CartRequestDTO cartRequestDTO = new CartRequestDTO();
        cartRequestDTO.setItems(List.of());

        assertThrows(InvalidCouponException.class,
                () -> couponService.applyCoupon(couponId, cartRequestDTO));
    }


    //STRATEGY FACTORY TESTS

    @Test
    void testFactory_productWise() {
        CouponStrategyFactory f =
                new CouponStrategyFactory(cartWiseStrategy, productWiseStrategy, bxGyStrategy);

        assertEquals(productWiseStrategy, f.getStrategy(CouponType.PRODUCT_WISE));
    }

    @Test
    void testFactory_cartWise() {
        CouponStrategyFactory f =
                new CouponStrategyFactory(cartWiseStrategy, productWiseStrategy, bxGyStrategy);

        assertEquals(cartWiseStrategy, f.getStrategy(CouponType.CART_WISE));
    }

    @Test
    void testFactory_bxgy() {
        CouponStrategyFactory f =
                new CouponStrategyFactory(cartWiseStrategy, productWiseStrategy, bxGyStrategy);

        assertEquals(bxGyStrategy, f.getStrategy(CouponType.BXGY));
    }

    @Test
    void testFactory_invalid() {
        CouponStrategyFactory f =
                new CouponStrategyFactory(cartWiseStrategy, productWiseStrategy, bxGyStrategy);

        // Map.of(...).get(null) throws NPE, so assert that
        assertThrows(NullPointerException.class,
                () -> f.getStrategy(null));
    }


    //PRODUCT WISE STRATEGY

    @Test
    void testProductWiseStrategy_calculateAndApply() {

        ProductWiseStrategy strat = new ProductWiseStrategy();

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setOriginalPrice(100.0);

        Cart cart = new Cart();
        cart.setItems(List.of(item));
        cart.setTotalAmount(200.0);

        Coupon coupon = new Coupon();
        coupon.setName("P10");
        coupon.setType(CouponType.PRODUCT_WISE);
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(10.0);
        coupon.setApplicableProductIds(List.of(100L));

        double discount = strat.calculateDiscount(coupon, cart);
        assertEquals(20.0, discount, 0.001);

        strat.applyDiscount(coupon, cart);
        assertEquals(20.0, item.getDiscountAmount(), 0.001);
        assertEquals(180.0, item.getDiscountedPrice(), 0.001);
    }


    //CART WISE STRATEGY


    @Test
    void testCartWiseStrategy_calculateAndApply() {

        CartWiseStrategy strat = new CartWiseStrategy();

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(3);
        item.setOriginalPrice(100.0);

        Cart cart = new Cart();
        cart.setItems(List.of(item));
        cart.setTotalAmount(300.0);

        Coupon coupon = new Coupon();
        coupon.setName("C20");
        coupon.setType(CouponType.CART_WISE);
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(20.0);
        coupon.setMinimumCartTotal(0.0);

        double discount = strat.calculateDiscount(coupon, cart);
        assertEquals(60.0, discount, 0.001);

        strat.applyDiscount(coupon, cart);
        assertEquals(60.0, item.getDiscountAmount(), 0.001);
        assertEquals(240.0, item.getDiscountedPrice(), 0.001);
    }


    // BXGY STRATEGY


    @Test
    void testBxGyStrategy_applyDoesNotCrash() {

        ProductRepository productRepositoryMock = mock(ProductRepository.class);
        BxGyStrategy strat = new BxGyStrategy(productRepositoryMock);

        Cart cart = new Cart();
        cart.setItems(List.of());
        cart.setTotalAmount(0.0);

        Coupon coupon = new Coupon();
        coupon.setName("BXGY");
        coupon.setType(CouponType.BXGY);
        coupon.setBuyQuantity(2);
        coupon.setGetQuantity(1);
        coupon.setBuyProductIds(List.of());
        coupon.setFreeProductIds(List.of());

        strat.applyDiscount(coupon, cart);
        assertTrue(true);
    }

}
