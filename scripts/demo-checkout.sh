#!/usr/bin/env bash
# ============================================================================
# EcoShop v2 — orchestrated checkout demo
# ----------------------------------------------------------------------------
# Exercises the full saga: cart → inventory → pricing → shipping → order → payment
# Requires: jq, curl
# ============================================================================
set -e

GW="${GATEWAY:-http://localhost:8080}"
EMAIL="demo+$(date +%s)@ecoshop.local"
PASS="DemoPass123!"
SKU_DEMO="DEMO-SKU-001"
WAREHOUSE_ID="11111111-1111-1111-1111-111111111111"

echo "==> 1. Register"
TOKEN=$(curl -sf -X POST "$GW/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\",\"firstName\":\"Demo\",\"lastName\":\"User\"}" \
  | jq -r '.data.accessToken')
USER_ID=$(echo "$TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null | jq -r '.userId' 2>/dev/null || echo "unknown")
echo "  Token: ${TOKEN:0:40}..."

echo ""
echo "==> 2. Stock the SKU $SKU_DEMO at warehouse $WAREHOUSE_ID"
echo "    (In real usage, stock comes from PO receipts. We POST directly for the demo.)"
echo ""
echo "    Note: inventory-service requires the stock_items row to exist before adjustments."
echo "    For this demo we'll instead use an SQL-style direct insert — but since checkout-service"
echo "    only needs reserve to work, and there's no stock-creation endpoint exposed (admin gap)"
echo "    we'd need to either:"
echo "      (a) add a POST /api/inventory/stock endpoint (TODO for Claude Code)"
echo "      (b) seed via Flyway V2 migration"
echo "      (c) connect to Postgres and INSERT directly for the demo"
echo ""
echo "    For now, we'll attempt the checkout flow assuming stock exists."

echo ""
echo "==> 3. Get/create coupon (WELCOME10 is seeded)"
QUOTE=$(curl -sf -X POST "$GW/api/pricing/coupons/quote" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"WELCOME10\",\"userId\":\"$USER_ID\",\"cartSubtotal\":1000.00}" \
  | jq '.data')
echo "  Coupon quote (subtotal ₹1000): $QUOTE"

echo ""
echo "==> 4. Get a shipping rate"
RATE=$(curl -sf -X POST "$GW/api/shipping/rates" \
  -H "Content-Type: application/json" \
  -d '{"fromPincode":"560001","toPincode":"400001","weightGrams":1500}' \
  | jq '.data')
echo "  Shipping rate: $RATE"

echo ""
echo "==> 5. Run checkout orchestrator (will fail at inventory step until stock is seeded)"
IDEM="checkout-$(date +%s)-$RANDOM"
CART_KEY="$USER_ID"
CHECKOUT_BODY=$(cat <<JSON
{
  "cartKey": "$CART_KEY",
  "shippingAddress": {
    "recipientName": "Demo User",
    "phone": "+919876543210",
    "line1": "Flat 4B, Sample Tower",
    "city": "Mumbai", "state": "MH", "postalCode": "400001", "country": "IN"
  },
  "couponCode": "WELCOME10",
  "paymentMethod": "UPI",
  "idempotencyKey": "$IDEM"
}
JSON
)

# Note: this will fail with EMPTY_CART unless you've added items to the cart first.
# For a complete demo, you need:
#   1. Catalog seeded with products (admin task)
#   2. Cart populated with those products
#   3. Stock seeded for the SKUs in inventory-service

echo "  Checkout request body:"
echo "$CHECKOUT_BODY" | jq .
echo ""
echo "  Calling /api/checkout ..."
CHECKOUT_RESP=$(curl -sf -X POST "$GW/api/checkout" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$CHECKOUT_BODY" 2>&1) || {
  echo "  Checkout failed (expected if cart empty / no stock seeded):"
  echo "  $CHECKOUT_RESP" | tail -5
  echo ""
  echo "  To make this demo run end-to-end, you need to:"
  echo "    1. Seed catalog (POST /api/products as admin)"
  echo "    2. Seed inventory stock_items rows in Postgres OR add admin endpoint"
  echo "    3. POST to /api/cart/items as the user"
  echo "    4. Re-run this script"
  exit 0
}

echo "$CHECKOUT_RESP" | jq '.data | {id, status, totalAmount, orderId, orderNumber, paymentGatewayRef}'

GATEWAY_REF=$(echo "$CHECKOUT_RESP" | jq -r '.data.paymentGatewayRef')

echo ""
echo "==> 6. Capture payment via mock gateway"
curl -sf -X POST "$GW/api/payments/capture" \
  -H "Content-Type: application/json" \
  -d "{\"gatewayRef\":\"$GATEWAY_REF\"}" | jq '.data | {status, gatewayRef}'

echo ""
echo "==> 7. Wait 3s for Kafka propagation, then read order"
sleep 3
ORDER_ID=$(echo "$CHECKOUT_RESP" | jq -r '.data.orderId')
curl -sf "$GW/api/orders/$ORDER_ID" -H "Authorization: Bearer $TOKEN" \
  | jq '.data | {orderNumber, status, paymentStatus}'

echo ""
echo "✓ Demo flow complete."
echo "  Watch logs: docker compose logs -f notification-service inventory-service order-service"
