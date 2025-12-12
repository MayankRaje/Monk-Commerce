
#  MonkCommerce - Coupon Management System

**Spring Boot** coupon engine with **Strategy + Factory + Repository** patterns supporting **Cart-wise**, **Product-wise**, & **BxGy** discounts.

##  Architecture
CouponService → CouponStrategyFactory → CouponDiscountStrategy (3 impls)

[CartRepository] [ProductRepository] [Cart 1→* CartItem → Product]


## Quick Start
mvn spring-boot:run

API: http://localhost:8080

H2 Console: http://localhost:8080/h2-console (sa / empty)

---

##  IMPLEMENTED CASES

### 1. **Cart-wise Coupons**
- **Percentage/Fixed discounts** on total cart value
- **Minimum cart total validation** (`cartTotal > minimumCartTotal`)
- **Proportional discount distribution** across all items
- **Pre-application validation** via `checkApplicability()`

### 2. **Product-wise Coupons**
- **Multiple product support** via `applicableProductIds`
- **Percentage/Fixed discounts** per matching product
- **Non-applicable products** remain at original price
- **Precise discount calculation** per item

### 3. **BxGy Coupons**
- **Buy X get Y free** across multiple products
- **Repetition limits** via `maxRepetitions`
- **Quantity aggregation** across buy products
- **Smart free product distribution** (prioritizes available items)
- **Partial free quantity handling**

### 4. **Core Features**
| Feature | Implementation |
|---------|----------------|
| **Full CRUD** | `create/update/read/deleteCoupon()` |
| **Active/Expiry** | `isActive` + `startDate/endDate` validation |
| **Transactional** | `@Transactional` on `applyCoupon()` |
| **Format Support** | API format → Internal DTO conversion |
| **Error Handling** | Custom exceptions + validation |

---

##  UNIMPLEMENTED CASES

| Case | Reason | Priority |
|------|--------|----------|
| **Coupon Stacking** | Complex priority/conflict rules | High |
| **User-specific Coupons** | No authentication system | High |
| **Usage Limits** | No tracking tables | Medium |
| **Tiered Discounts** | Complex tier logic | Medium |
| **Category Discounts** | No category model | Low |
| **Coupon Codes** | ID-based identification | Low |

---

##  LIMITATIONS

| Limitation | Impact | Workaround |
|------------|--------|------------|
| **Single Coupon** | No stacking | Apply best coupon |
| **No Cart Persistence** | Stateless carts | Send full cart each request |
| **Double Precision** | Minor rounding | Acceptable for demo |
| **No Concurrency** | Race conditions | Single-threaded access |
| **H2 In-Memory** | Data lost on restart | Use for dev only |

---

##  ASSUMPTIONS

| Assumption | Rationale |
|------------|-----------|
| **Products exist** | Validated during application |
| **Single currency** | No multi-currency support |
| **Integer quantities** | Standard e-commerce |
| **Positive discounts** | Business rule |
| **Active coupons only** | Inactive excluded automatically |
| **Client sends full cart** | Stateless API design |

---

##  CLASS DIAGRAM
<img width="8125" height="5640" alt="ClassDiagram" src="https://github.com/user-attachments/assets/57d69d23-a1ed-4fe8-a3a3-40f771324db8" />


##  API Endpoints

CRUD

POST /coupons # {"type":"cart-wise","details":{"threshold":100,"discount":10}}

GET /coupons

GET /coupons/{id}

PUT /coupons/{id}

DEL /coupons/{id}

Cart Ops

POST /applicable-coupons

POST /apply-coupon/{id}


---
**BONUS**: Unit tests & expiration dates ready for implementation per requirements.

Implemented Cases: All 3 coupon types + core features

Unimplemented Cases: 6 key cases with reasons

Limitations: 5 major limitations + workarounds

Assumptions: 6 key assumptions documented
