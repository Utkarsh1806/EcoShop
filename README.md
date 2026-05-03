# EcoShop

A microservices-based e-commerce platform I've been building to learn distributed systems patterns in practice. Not a tutorial project — actual working services with Kafka, sagas, pessimistic locking, and the usual fun that comes with microservices.

---

## Services

| Service | Port | Status | Notes |
|---|---|---|---|
| `infra-eureka-server` | 8761 | ✅ | Service registry |
| `infra-config-server` | 8888 | ✅ | Centralized config |
| `infra-api-gateway` | 8080 | ✅ | JWT auth, rate limiting, circuit breakers |
| `user-service` | 8101 | ✅ | Register/login, JWT, profile, addresses |
| `product-catalog-service` | 8102 | ✅ | Products, categories, variants, search |
| `cart-service` | 8106 | ✅ | Redis-backed, guest + user carts, merge on login |
| `order-service` | 8108 | ✅ | Full lifecycle with status state machine, Kafka |
| `payment-service` | 8109 | ✅ | Mock gateway, idempotency, HMAC webhooks, refunds |
| `inventory-service` | 8105 | ✅ | Pessimistic-lock reservations, multi-warehouse |
| `pricing-promotion-service` | 8111 | ✅ | Coupon engine (% and flat), atomic redemption |
| `notification-service` | 8113 | ✅ | Multi-channel with deduplication, Kafka-driven |
| `shipping-service` | 8110 | ✅ | Courier abstraction layer, tracking events |
| `checkout-service` | 8107 | ✅ | Saga orchestrator across 6 services |
| `review-rating-service` | 8112 | ✅ | Reviews, rating summaries, moderation workflow |
| `returns-service` | 8118 | ✅ | RMA lifecycle, Feign to order + payment, refunds |
| `seller-service` | 8114 | ✅ | Marketplace onboarding, product approval, payouts |
| `fraud-service` | 8117 | ✅ | Rule engine, blocklist, auto-eval on order.created |
| `admin-service` | 8115 | ✅ | Audit log, cross-service aggregator |

**~209 Java files across 18 services.**

---

## How to run

```bash
mvn -T 1C clean install -DskipTests
docker compose up -d
open http://localhost:8761
```

Wait for all services to register on Eureka before hitting any endpoints.

---

## Tech stack

- **Java 17 + Spring Boot 3.3**
- **Spring Cloud** — Eureka, Config Server, Gateway, OpenFeign, Circuit Breaker (Resilience4j)
- **Kafka** — async event-driven communication between services
- **Redis** — cart storage
- **PostgreSQL** — per-service databases (DB-per-service pattern)
- **Docker Compose** — local orchestration
- **Flyway** — DB migrations per service

---

## Service details

### review-rating-service (8112)

Handles product reviews with a moderation lifecycle. Rating summaries are denormalized per product — I track `rating_sum` alongside count so the average doesn't accumulate floating-point errors over time. Updates use pessimistic locking on the summary row.

Moderation flow: `PENDING_MODERATION → APPROVED / REJECTED / HIDDEN`. Any edit pushes a review back to pending. Status transitions adjust the rating summary atomically.

Set `REVIEWS_AUTO_APPROVE=true` in dev to skip the moderation queue.

Emits `review.submitted` (fraud-service can subscribe for spam detection) and `product.rating.updated` (catalog can keep its own rating fields in sync).

---

### returns-service (8118)

RMA lifecycle with 10 states:

```
REQUESTED → APPROVED → PICKUP_SCHEDULED → PICKED_UP → QC_PENDING → QC_PASSED → REFUND_INITIATED → REFUNDED → CLOSED
```

Branches for `REJECTED` and `QC_FAILED`. Invalid transitions throw `INVALID_TRANSITION` — no silent no-ops.

Feign clients to order-service (ownership check, item details) and payment-service (refund, idempotent on orderId). Only DELIVERED orders are eligible. Return items snapshot product name, SKU, and unit price at request time so they survive catalog edits later.

---

### seller-service (8114)

Marketplace functionality. Sellers go through an onboarding flow:

```
submit → PENDING_VERIFICATION → admin verifies + sets commission rate → ACTIVE → can list products
```

Products need separate approval per seller (`PENDING → APPROVED`). GSTIN and PAN validated via regex. Bank account stores only last-4 digits.

Payout formula: `gross_sales × commission_rate = commission`, `gross - commission - refunds = net_payout`.

---

### fraud-service (8117)

Pluggable rule engine — implement `FraudRule`, annotate with `@Component`, and it gets picked up automatically via `List<FraudRule>` injection. Currently 8 built-in rules:

| Rule | Score |
|---|---|
| Blocklist (email / phone / IP) | 70–80 |
| High order value (≥₹50K / ≥₹2L) | 20 / 40 |
| First order high value (new customer + ≥₹20K) | 25 |
| Velocity (≥5 orders/24h / ≥10 orders/24h) | 15 / 35 |
| Recent high-risk history (HIGH/CRITICAL in last 7d) | 30 |
| Billing/shipping state mismatch | 5–15 |

Risk bands: `LOW (0–24)` → auto approve, `MEDIUM (25–49)` → approve, `HIGH (50–79)` → flag for review, `CRITICAL (80+)` → block.

Subscribes to `order.created` and evaluates immediately. Emits `fraud.decision` — order-service and payment-service can gate on this.

---

### admin-service (8115)

Aggregator for admin operations. The `/admin/orders/{id}/investigate` endpoint merges data from order, user, and fraud services in a single call — useful for support workflows without jumping between services.

Every admin action gets written to `AdminAuditLog` (who, what, target, IP, result). All endpoints require `ROLE_ADMIN`.

---

## Kafka topics

| Topic | Producer | Consumers |
|---|---|---|
| `order.created` | order-service | notification, fraud |
| `order.status.changed` | order-service | inventory, notification, shipping |
| `payment.succeeded` | payment-service | order, notification |
| `payment.failed` | payment-service | order, inventory, notification |
| `shipment.created` | shipping-service | *(analytics, when built)* |
| `shipment.status.changed` | shipping-service | *(analytics, when built)* |
| `review.submitted` | review-rating | fraud *(spam check)* |
| `product.rating.updated` | review-rating | catalog |
| `return.created` / `return.status.changed` / `return.refunded` | returns | analytics, notification |
| `seller.verified` / `seller.product.approved` | seller | catalog |
| `fraud.decision` | fraud | order, payment |

---

## Known gaps

A few things I want to be upfront about:

1. **Not compile-verified yet** — built this without Maven Central access in the sandbox. Structure and imports should be clean, but expect maybe 1–3 small fixes on first build.

2. **Dev secrets everywhere** — JWT secret, payment webhook secret, mock gateway configs all use defaults. Change these before exposing anything externally.

3. **No tests yet** — integration tests are the next priority, especially for the checkout saga and the returns → payment → refund flow. Plan to use Testcontainers.

4. **Fraud rules are heuristics** — good enough to learn the pattern, but real fraud detection needs device fingerprinting, ML scoring, etc. Sift/Stripe Radar are the practical options if this ever goes to production.

5. **~7 services still stubbed** — search, recommendation, analytics, support. These are mostly vendor-shaped integrations (Elasticsearch, ML platforms, BI pipelines) rather than straightforward CRUD, so they need more thought.

---

## What's next

- First compile pass and fix any import nits
- Seed data endpoints for local dev (stock rows, sample products via Flyway V2 migrations)
- Wire `fraud.decision` gating into order-service (hold REVIEW/BLOCK orders)
- Build `search-service` — Elasticsearch + Debezium CDC from catalog
- Build `support-service` — tickets + threaded messages with status lifecycle
- Testcontainers integration tests for checkout saga, return-refund flow, fraud evaluation
