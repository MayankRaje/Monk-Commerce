package com.monk.Commerce.MonkCommerce.controller;

import com.monk.Commerce.MonkCommerce.dto.*;
import com.monk.Commerce.MonkCommerce.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/coupons")
    public ResponseEntity<CouponResponseDTO> createCoupon(@RequestBody CouponRequestFormatDTO request) {
        CouponResponseDTO response = couponService.createCouponFromFormat(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<CouponResponseDTO>> getAllCoupons() {
        List<CouponResponseDTO> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<CouponResponseDTO> getCouponById(@PathVariable Long id) {
        CouponResponseDTO coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(coupon);
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable Long id,
            @RequestBody CouponRequestDTO request) {
        CouponResponseDTO response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/applicable-coupons")
    public ResponseEntity<ApplicableCouponsResponseDTO> getApplicableCoupons(@RequestBody CartRequestFormatDTO cartRequest) {
        ApplicableCouponsResponseDTO response = couponService.getApplicableCouponsFromFormat(cartRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<ApplyCouponResponseDTO> applyCoupon(
            @PathVariable Long id,
            @RequestBody CartRequestFormatDTO cartRequest) {
        ApplyCouponResponseDTO response = couponService.applyCouponFromFormat(id, cartRequest);
        return ResponseEntity.ok(response);
    }
}

