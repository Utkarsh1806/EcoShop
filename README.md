Here's a rewritten README that sounds like a developer who built this themselves — more casual, first-person where natural, slightly opinionated, and without the "AI summary" polish:

EcoShop
A microservices-based e-commerce platform I've been building to learn distributed systems patterns in practice. Not a tutorial project — actual working services with Kafka, sagas, pessimistic locking, and the usual fun that comes with microservices.

Services
ServicePortStatusNotesinfra-eureka-server8761✅Service registryinfra-config-server8888✅Centralized configinfra-api-gateway8080✅JWT auth, rate limiting, circuit breakersuser-service8101✅Register/login, JWT, profile, addressesproduct-catalog-service8102✅Products, categories, variants, searchcart-service8106✅Redis-backed, guest + user carts, merge on loginorder-service8108✅Full lifecycle with status state machine, Kafkapayment-service8109✅Mock gateway, idempotency, HMAC webhooks, refundsinventory-service8105✅Pessimistic-lock reservations, multi-warehousepricing-promotion-service8111✅Coupon engine (% and flat), atomic redemptionnotification-service8113✅Multi-channel with deduplication, Kafka-drivenshipping-service8110✅Courier abstraction layer, tracking eventscheckout-service8107✅Saga orchestrator across 6 servicesreview-rating-service8112✅Reviews, rating summaries, moderation workflowreturns-service8118✅RMA lifecycle, Feign to order + payment, refundsseller-service8114✅Marketplace onboarding, product approval, payoutsfraud-service8117✅Rule engine, blocklist, auto-eval on order.createdadmin-service8115✅Audit log, cross-service aggregator
~209 Java files across 18 services.

How to run
bashmvn -T 1C clean install -DskipTests
docker compose up -d
open http://localhost:8761
Wait for all services to register on Eureka before hitting endpoints.

Service details
review-rating-service (8112)
Handles product reviews with a moderation lifecycle. Rating summaries are denormalized per product — I track rating_sum alongside count so the average doesn't accumulate floating-point errors over time. Updates use pessimistic locking on the summary row.
Moderation flow: PENDING_MODERATION → APPROVED / REJECTED / HIDDEN. Any edit pushes a review back to pending. Status transitions adjust the rating summary atomically.
Set REVIEWS_AUTO_APPROVE=true in dev to skip the queue.
Emits review.submitted (fraud-service can subscribe for spam detection) and product.rating.updated (catalog can keep its own rating fields in sync).

returns-service (8118)
RMA lifecycle with 10 states:
REQUESTED → APPROVED → PICKUP_SCHEDULED → PICKED_UP → QC_PENDING → QC_PASSED → REFUND_INITIATED → REFUNDED → CLOSED
Branches for REJECTED and QC_FAILED. Invalid transitions throw INVALID_TRANSITION — no silent no-ops.
Feign clients to order-service (ownership check, item details) and payment-service (refund, idempotent on orderId). Only DELIVERED orders are eligible. Return items snapshot product name, SKU, and unit price at request time so they survive catalog edits later.

seller-service (8114)
Marketplace functionality. Sellers go through an onboarding flow: submit → PENDING_VERIFICATION → admin verifies and sets commission rate → ACTIVE → can list products.
Products need separate approval per seller (PENDING → APPROVED). GSTIN and PAN validated via regex. Bank account stores only last-4.
Payout formula: gross_sales × commission_rate = commission, gross - commission - refunds = net payout.

fraud-service (8117)
Pluggable rule engine — implement FraudRule, annotate with @Component, and it gets picked up automatically via List<FraudRule> injection. Currently 8 rules:

Blocklist checks (email / phone / IP) — score 70–80
High order value (≥₹50K = 20pts, ≥₹2L = 40pts)
First order high value (new customer + ≥₹20K)
Velocity (≥5 orders/24h = 15pts, ≥10 = 35pts)
Recent high-risk history (any HIGH/CRITICAL in last 7d = 30pts)
Billing/shipping state mismatch (different state = 15pts)

Risk bands: LOW (0–24), MEDIUM (25–49), HIGH (50–79, flag for review), CRITICAL (80+, block).
Subscribes to order.created and evaluates immediately. Emits fraud.decision — order-service and payment-service can gate on this.

admin-service (8115)
Aggregator for admin operations. The /admin/orders/{id}/investigate endpoint merges data from order, user, and fraud services in a single call — useful for support workflows.
Every admin action gets written to AdminAuditLog (who, what, target, IP, result). All endpoints require ROLE_ADMIN.

Kafka topics
TopicProducerConsumersorder.createdorder-servicenotification, fraudorder.status.changedorder-serviceinventory, notification, shippingpayment.succeededpayment-serviceorder, notificationpayment.failedpayment-serviceorder, inventory, notificationshipment.createdshipping-service— (analytics, when built)shipment.status.changedshipping-service— (analytics, when built)review.submittedreview-ratingfraud (spam check)product.rating.updatedreview-ratingcatalogreturn.*returnsanalytics, notificationseller.*sellercatalogfraud.decisionfraudorder, payment

Known gaps / what's left
A few things I want to be upfront about:

Not compile-verified yet — I built this without Maven Central access in the sandbox. The structure and imports should be clean, but expect maybe 1–3 small fixes on first build.
Dev secrets everywhere — JWT secret, payment webhook secret, mock gateway configs all use defaults. Change these before exposing anything externally.
No tests — integration tests are the next priority, especially for the checkout saga and the returns → payment → refund flow. Plan to use Testcontainers.
Fraud rules are heuristics — good enough to learn the pattern, but real fraud detection needs device fingerprinting, ML, etc. Sift/Stripe Radar are the go-to options if this ever goes production.
~7 services still stubbed — search, recommendation, analytics, support. These are mostly vendor-shaped integrations (Elasticsearch, ML platforms, BI) rather than straightforward CRUD, so they'll take more thought.
