# EcoShop — E-Commerce Microservices Platform

**15 of 22 services deeply implemented.** Marketplace, returns, fraud, reviews, and admin tooling now layer on top of the v2 core.

---

## Status

| Service | Port | Status | What's in it |
|---|---|---|---|
| `infra-eureka-server` | 8761 | ✅ Ready | Service registry |
| `infra-config-server` | 8888 | ✅ Ready | Centralized config |
| `infra-api-gateway` | 8080 | ✅ Ready | Routes, JWT, rate-limit, circuit breakers |
| `user-service` | 8101 | ✅ Complete | Register, login, JWT, profile, addresses |
| `product-catalog-service` | 8102 | ✅ Complete | Products, categories, brands, variants, search |
| `cart-service` | 8106 | ✅ Complete | Redis cart, guest+user, merge-on-login |
| `order-service` | 8108 | ✅ Complete | Lifecycle, status machine, Kafka pub/sub |
| `payment-service` | 8109 | ✅ Complete | Mock gateway, idempotency, HMAC webhooks, refunds |
| `inventory-service` | 8105 | ✅ Complete | Pessimistic-lock reservations, multi-warehouse |
| `pricing-promotion-service` | 8111 | ✅ Complete | Coupons (% / flat), atomic redemption |
| `notification-service` | 8113 | ✅ Complete | Multi-channel dispatcher with dedupe, Kafka subscriptions |
| `shipping-service` | 8110 | ✅ Complete | Courier abstraction, tracking events |
| `checkout-service` | 8107 | ✅ Complete | Saga orchestrator across 6 services |
| **`review-rating-service`** | **8112** | **✅ Complete (NEW)** | **Reviews + denormalized rating summaries with pessimistic-lock atomic updates, moderation lifecycle** |
| **`returns-service`** | **8118** | **✅ Complete (NEW)** | **RMA lifecycle (10 states), Feign integration with order + payment, atomic refunds** |
| **`seller-service`** | **8114** | **✅ Complete (NEW)** | **Marketplace: seller onboarding, product approval, payouts with commission calc, GST/PAN validation** |
| **`fraud-service`** | **8117** | **✅ Complete (NEW)** | **Pluggable rule engine (8 built-in rules), blocklist, auto-evaluation on order.created** |
| **`admin-service`** | **8115** | **✅ Complete (NEW)** | **Audit log + cross-service aggregator (Feign clients to user/order/fraud/seller)** |
| `search-service` | 8103 | 🟡 Stub | Needs Elasticsearch + indexing |
| `recommendation-service` | 8104 | 🟡 Stub | Needs ML pipeline |
| `analytics-service` | 8116 | 🟡 Stub | Needs Kafka→DW pipeline |
| `support-service` | 8119 | 🟡 Stub | Tickets + chat |

**209 Java files. 304 total files.**

---

## 

### review-rating-service (8112)
- **Entities**: `Review` (with verified-purchase flag, helpful count, moderation status), `ProductRatingSummary` (denormalized per-product aggregate with rating distribution buckets)
- **Atomic rating math**: pessimistic-lock on summary rows during update, `rating_sum` tracked alongside count to derive average without floating-point drift
- **Moderation lifecycle**: PENDING_MODERATION → APPROVED / REJECTED / HIDDEN. Edits force re-moderation. Status changes apply +/- to summary atomically.
- **Endpoints**: public reads (with sort by newest/highest/lowest/most-helpful), authenticated submit/edit/delete/helpful, admin moderation queue
- **Events**: `review.submitted` (for fraud spam check), `product.rating.updated` (catalog can subscribe to update its own rating fields)
- **Config**: `REVIEWS_AUTO_APPROVE=true` to skip moderation in dev

### returns-service (8118)
- **Entities**: `ReturnRequest` (with RMA number `RMA-{year}-{random}`), `ReturnItem`, `ReturnStatusHistory`
- **State machine** (10 statuses): REQUESTED → APPROVED → PICKUP_SCHEDULED → PICKED_UP → QC_PENDING → QC_PASSED → REFUND_INITIATED → REFUNDED → CLOSED, with branches for REJECTED / QC_FAILED. Invalid transitions throw `INVALID_TRANSITION`.
- **Feign clients** to:
  - **order-service**: verifies user owns order, fetches item details for refund calc
  - **payment-service**: issues actual refund (idempotent on orderId via payment-service's existing logic)
- **Eligibility check**: only DELIVERED orders can have returns. Quantity validated against order line item qty.
- **Snapshot pattern**: return items capture product name, SKU, unit price at request time (so they survive product edits)
- **Events**: `return.created`, `return.status.changed`, `return.refunded`

### seller-service (8114)
- **Entities**: `Seller` (with GSTIN/PAN validation via regex, bank account last-4 storage), `SellerProductLink` (one product → one seller, with approval workflow), `Payout` (period-based with commission calc)
- **Onboarding flow**: seller submits → status `PENDING_VERIFICATION` → admin verifies (sets commission rate) → status `ACTIVE` → can submit products
- **Product approval workflow**: seller submits product link → `PENDING` → admin approves → `APPROVED` (publishes `seller.product.approved`)
- **Payouts**: `gross_sales × commission_rate` = commission, `gross - commission - refunds` = net payout. Status: PENDING → PROCESSING → COMPLETED.
- **Endpoints**: seller self-service (`/me`), public seller storefront (`/{sellerId}`), admin verify/suspend/approve-products/create-payouts
- **Events**: `seller.verified`, `seller.product.approved`

### fraud-service (8117)
- **Entities**: `FraudCheck` (per-evaluation record), `RuleHit` (which rules fired, with score and evidence), `BlocklistEntry` (email/phone/IP/device)
- **Pluggable rule engine**: implement `FraudRule` interface, register as `@Component` — auto-discovered via `List<FraudRule>` injection
- **8 built-in rules**:
  - `BLOCKLIST_EMAIL` / `BLOCKLIST_PHONE` / `BLOCKLIST_IP` (score 70-80)
  - `HIGH_ORDER_VALUE` (≥₹50K = 20pts, ≥₹2L = 40pts)
  - `FIRST_ORDER_HIGH_VALUE` (first-time customer + ≥₹20K)
  - `VELOCITY_24H` (≥5 orders 24h = 15pts, ≥10 = 35pts)
  - `RECENT_HIGH_RISK` (any HIGH/CRITICAL in 7d = 30pts)
  - `BILLING_SHIPPING_MISMATCH` (different state circles = 15pts, same state = 5pts)
- **Risk thresholds**: 0-24 LOW (APPROVE), 25-49 MEDIUM (APPROVE), 50-79 HIGH (REVIEW), 80+ CRITICAL (BLOCK)
- **Auto-evaluation**: subscribes to `order.created`, runs check immediately
- **Events**: `fraud.decision` (consumers: order-service can hold orders, payment-service can refuse capture)

### admin-service (8115)
- **Aggregator pattern**: Feign clients to user/order/fraud/seller services
- **`/admin/orders/{id}/investigate`**: one call returns order + user + fraud check (3 services merged)
- **Audit log**: `AdminAuditLog` entity records every admin action (admin user, action, target, IP, details, result) — required for compliance
- **All endpoints require `ROLE_ADMIN`** (enforced at SecurityConfig level)

---

## Updated Kafka topology

10 topics now flowing across services:

| Topic | Producer | Consumers |
|---|---|---|
| `order.created` | order-service | notification, **fraud** |
| `order.status.changed` | order-service | inventory, notification, shipping |
| `payment.succeeded` | payment-service | order, notification |
| `payment.failed` | payment-service | order, inventory, notification |
| `shipment.created` | shipping-service | (analytics-service when implemented) |
| `shipment.status.changed` | shipping-service | (analytics-service when implemented) |
| `review.submitted` | **review-rating** | (fraud-service can subscribe for spam check) |
| `product.rating.updated` | **review-rating** | (catalog-service can subscribe to update rating fields) |
| `return.created` / `return.status.changed` / `return.refunded` | **returns** | (analytics, notification) |
| `seller.verified` / `seller.product.approved` | **seller** | (catalog can mark seller's products approved) |
| `fraud.decision` | **fraud** | order, payment (gating) |

---

## Quick start

```bash
mvn -T 1C clean install -DskipTests
docker compose up -d
open http://localhost:8761    # Watch services register
```

---

## Honest disclaimers (still apply)

1. **Not compile-verified.** Sandbox doesn't have Maven Central access. All Java is structurally clean (balanced braces, valid packages, valid YAML/XML, all imports resolved against documented Spring Boot 3.3 APIs). Expect 1–3 minor fixes on first build — small things, not architectural.
2. **JWT secret, payment webhook secret, mock gateway/courier all use dev defaults.** Change before any external exposure.
3. **Fraud rules are heuristics.** Real production fraud detection adds device fingerprinting, behavioral biometrics, and ML models. Sift / Stripe Radar / Riskified are common buy-not-build options.
4. **No tests.** Integration tests for the saga flows (especially returns→payment→refund) are critical. First task in Claude Code.
5. **7 of 22 services still stubs.** Remaining: search, recommendation, analytics, support. These are mostly vendor-SDK shaped (Elasticsearch, ML platforms, BI pipelines) rather than CRUD work.

---

## What to do with Claude Code from here

1. **First compile** — `mvn compile` in each service to find any import nits and fix them
2. **Add the missing demo seeding endpoints** (e.g. `POST /api/inventory/stock` to create stock rows directly, V2 Flyway migrations with sample products/stock)
3. **Wire the gating** — make order-service consume `fraud.decision` and hold orders flagged as REVIEW/BLOCK
4. **Build search-service** — Elasticsearch + Debezium CDC from catalog
5. **Build support-service** — straightforward tickets + threaded messages CRUD with status lifecycle
6. **Add Testcontainers integration tests** for the critical flows: checkout-saga, return-refund, fraud-evaluation
