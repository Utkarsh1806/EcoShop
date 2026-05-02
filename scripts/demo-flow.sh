#!/usr/bin/env bash
# ============================================================================
# EcoShop end-to-end demo
# ----------------------------------------------------------------------------
# Exercises register → browse → cart → checkout → payment flow
# Requires: jq, curl
# Run after: docker compose up -d (and waiting ~60s for services to register)
# ============================================================================
set -e

GW="${GATEWAY:-http://localhost:8080}"
EMAIL="demo+$(date +%s)@ecoshop.local"
PASS="DemoPass123!"

echo "==> 1. Register"
TOKEN=$(curl -s -X POST "$GW/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\",\"firstName\":\"Demo\",\"lastName\":\"User\"}" \
  | jq -r '.data.accessToken')
echo "  Token: ${TOKEN:0:40}..."

echo "==> 2. Get profile"
curl -s "$GW/api/users/me" -H "Authorization: Bearer $TOKEN" | jq '.data | {id, email, firstName}'

echo "==> 3. Add an address"
ADDR_ID=$(curl -s -X POST "$GW/api/users/me/addresses" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "label":"HOME",
    "recipientName":"Demo User",
    "phone":"+919876543210",
    "line1":"Flat 4B, Sample Tower",
    "city":"Mumbai", "state":"MH", "postalCode":"400001", "country":"IN",
    "isDefault": true
  }' | jq -r '.data.id')
echo "  Address ID: $ADDR_ID"

echo "==> 4. Admin: create a category (using same token, but in real flow this needs an ADMIN role)"
echo "    Skipping — requires an ADMIN user. Seed data in V2 migration recommended."
echo ""
echo "    For demo, assume catalog has been seeded. Listing first product:"
PRODUCT_ID=$(curl -s "$GW/api/products?size=1" | jq -r '.data.content[0].id // empty')
if [ -z "$PRODUCT_ID" ]; then
  echo "    No products in catalog. Run scripts/seed-demo-data.sh first or add via admin."
  echo "    Continuing with a fake productId — cart add WILL fail at the catalog lookup, which is correct behavior."
  PRODUCT_ID="00000000-0000-0000-0000-000000000001"
fi
echo "  Product ID: $PRODUCT_ID"

echo "==> 5. Add to cart"
CART=$(curl -s -X POST "$GW/api/cart/items" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":2}")
echo "$CART" | jq '.data | {itemCount, subtotal, items: (.items | length)}'

echo "==> 6. Place an order (constructed manually — in production checkout-service does this)"
ORDER=$(curl -s -X POST "$GW/api/orders" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{
    \"items\":[{
      \"productId\":\"$PRODUCT_ID\",
      \"productName\":\"Demo Product\",
      \"unitPrice\":499.00,
      \"quantity\":2
    }],
    \"shippingAddress\":{
      \"recipientName\":\"Demo User\",
      \"phone\":\"+919876543210\",
      \"line1\":\"Flat 4B\",
      \"city\":\"Mumbai\",\"state\":\"MH\",\"postalCode\":\"400001\",\"country\":\"IN\"
    },
    \"shippingCost\":40.00,
    \"tax\":89.82,
    \"currency\":\"INR\"
  }")
ORDER_ID=$(echo "$ORDER" | jq -r '.data.id')
ORDER_NUM=$(echo "$ORDER" | jq -r '.data.orderNumber')
TOTAL=$(echo "$ORDER" | jq -r '.data.totalAmount')
echo "  Order: $ORDER_NUM (id=$ORDER_ID, total=$TOTAL)"

echo "==> 7. Create a payment (mock gateway)"
IDEMP="idem-$(date +%s)-$RANDOM"
PAYMENT=$(curl -s -X POST "$GW/api/payments" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{
    \"orderId\":\"$ORDER_ID\",
    \"amount\":$TOTAL,
    \"currency\":\"INR\",
    \"method\":\"UPI\",
    \"idempotencyKey\":\"$IDEMP\"
  }")
GW_REF=$(echo "$PAYMENT" | jq -r '.data.gatewayRef')
echo "  Payment created. gatewayRef=$GW_REF"

echo "==> 8. Capture payment (frontend would do this after gateway redirect)"
CAPTURED=$(curl -s -X POST "$GW/api/payments/capture" \
  -H "Content-Type: application/json" \
  -d "{\"gatewayRef\":\"$GW_REF\"}")
echo "$CAPTURED" | jq '.data | {status, gateway, gatewayRef}'

echo ""
echo "==> 9. Wait 2s for Kafka event propagation, then check order status"
sleep 2
curl -s "$GW/api/orders/$ORDER_ID" -H "Authorization: Bearer $TOKEN" \
  | jq '.data | {orderNumber, status, paymentId, paymentStatus}'

echo ""
echo "✓ Demo flow complete. Watch logs: docker compose logs -f order-service payment-service"
