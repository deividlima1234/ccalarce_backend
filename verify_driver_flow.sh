#!/bin/bash

# Configuration
BASE_URL="https://ccalarce-backend.onrender.com/api/v1"
SA_USER="admin"
SA_PASS="admin123"

# Genera IDs únicos para evitar colisiones
TS=$(date +%s)
TS_SUFFIX=${TS: -4}

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}=== DRIVER WORKFLOW VERIFICATION ===${NC}"

# ==========================================
# PHASE 1: PRE-REQUISITES (ADMIN WORK)
# ==========================================
echo -e "\n${YELLOW}[PHASE 1] Setting up Environment (Admin Work)...${NC}"

# 1. Login SA
SA_TOKEN=$(curl -s -X POST "$BASE_URL/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$SA_USER\", \"password\": \"$SA_PASS\"}" | jq -r '.token')

if [ "$SA_TOKEN" == "null" ]; then echo -e "${RED}SA Login Failed${NC}"; exit 1; fi

# 2. Create Fresh Driver
DRIVER_USER="driver_$TS"
DRIVER_PASS="driver123"
DRIVER_ID=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{ \"fullName\": \"Test Driver $TS\", \"username\": \"$DRIVER_USER\", \"password\": \"$DRIVER_PASS\", \"role\": \"REPARTIDOR\" }" | jq -r '.id')  
# Note: Currently register returns Token, not ID immediately in JSON (AuthResponse). 
# We need to find the ID via User List or wait.
# Actually AuthenticationController.register returns AuthenticationResponse (token).
# To get ID, we need to list users.

DRIVER_ID=$(curl -s -X GET "$BASE_URL/users" \
  -H "Authorization: Bearer $SA_TOKEN" | jq -r ".[] | select(.username==\"$DRIVER_USER\") | .id")
echo "  > Created Driver: $DRIVER_USER (ID: $DRIVER_ID)"

# 3. Create Master Data
# Product
PRODUCT_ID=$(curl -s -X POST "$BASE_URL/products" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Balon Gas 10kg", "price": 50.00, "stock": 0}' | jq -r '.id')

curl -s -X POST "$BASE_URL/inventory/movement" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": $PRODUCT_ID, \"quantity\": 500, \"type\": \"PURCHASE\", \"reason\": \"Setup\"}" > /dev/null

# Vehicle
VEHICLE_ID=$(curl -s -X POST "$BASE_URL/vehicles" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{ \"plate\": \"D-$TS_SUFFIX\", \"capacity\": 100, \"active\": true }" | jq -r '.id')

# Client
CLIENT_ID=$(curl -s -X POST "$BASE_URL/clients" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{ \"fullName\": \"Client $TS\", \"documentNumber\": \"10$TS\", \"address\": \"Street $TS\", \"latitude\": -12.0, \"longitude\": -77.0 }" | jq -r '.id')

echo "  > Environment Ready: Product $PRODUCT_ID, Vehicle $VEHICLE_ID, Client $CLIENT_ID"

# 4. Open Route for Driver
ROUTE_ID=$(curl -s -X POST "$BASE_URL/routes/open" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"driverId\": $DRIVER_ID,
    \"vehicleId\": $VEHICLE_ID,
    \"stock\": [ { \"productId\": $PRODUCT_ID, \"quantity\": 20 } ]
  }" | jq -r '.id')
echo -e "  > ${GREEN}Route Opened by Admin. Route ID: $ROUTE_ID${NC}"


# ==========================================
# PHASE 2: DRIVER WORKFLOW
# ==========================================
echo -e "\n${YELLOW}[PHASE 2] Starting Driver Actions...${NC}"

# 1. Driver Login
echo "1. Logging in as Driver..."
DRIVER_TOKEN=$(curl -s -X POST "$BASE_URL/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$DRIVER_USER\", \"password\": \"$DRIVER_PASS\"}" | jq -r '.token')

if [ -z "$DRIVER_TOKEN" ] || [ "$DRIVER_TOKEN" == "null" ]; then
    echo -e "${RED}Driver Login Failed${NC}"
    exit 1
fi
echo -e "${GREEN}  > Login Successful${NC}"

# 2. Get Current Route (Tunnel Vision)
echo "2. Fetching My Route (Tunnel Vision)..."
MY_ROUTE_JSON=$(curl -s -X GET "$BASE_URL/routes/current" \
  -H "Authorization: Bearer $DRIVER_TOKEN")

FETCHED_ID=$(echo $MY_ROUTE_JSON | jq -r '.id')
INITIAL_STOCK=$(echo $MY_ROUTE_JSON | jq -r ".stock[] | select(.product.id==$PRODUCT_ID) | .currentQuantity")

if [ "$FETCHED_ID" == "$ROUTE_ID" ]; then
    echo -e "${GREEN}  > Success: Retrieved Correct Route ID $FETCHED_ID${NC}"
else
    echo -e "${RED}  > Fail: Expected $ROUTE_ID, got $FETCHED_ID${NC}"
fi
echo "  > Initial Stock on Truck: $INITIAL_STOCK"

# 3. Register Sale
echo "3. Registering Sale (2 units)..."
SALE_RES=$(curl -s -X POST "$BASE_URL/sales" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"routeId\": $FETCHED_ID,
    \"clientId\": $CLIENT_ID,
    \"paymentMethod\": \"CASH\",
    \"latitude\": -12.123,
    \"longitude\": -77.123,
    \"items\": [ { \"productId\": $PRODUCT_ID, \"quantity\": 2 } ]
  }")
SALE_ID=$(echo $SALE_RES | jq -r '.id')
echo -e "${GREEN}  > Sale Registered. ID: $SALE_ID${NC}"

# 4. Verify Stock Update (Tunnel Vision again)
echo "4. Verifying Stock Deduction..."
UPDATED_ROUTE=$(curl -s -X GET "$BASE_URL/routes/current" \
  -H "Authorization: Bearer $DRIVER_TOKEN")
NEW_STOCK=$(echo $UPDATED_ROUTE | jq -r ".stock[] | select(.product.id==$PRODUCT_ID) | .currentQuantity")

echo "  > Previous Stock: $INITIAL_STOCK"
echo "  > Current Stock:  $NEW_STOCK"

if [ "$NEW_STOCK" == "18" ]; then
    echo -e "${GREEN}  > Success: Stock correctly reduced by 2${NC}"
else
    echo -e "${RED}  > Fail: Expected 18, found $NEW_STOCK${NC}"
fi

# 5. Close Route (Liquidation)
echo "5. Closing Route (Reporting 18 units returned)..."
LIQ_RES=$(curl -s -X POST "$BASE_URL/liquidation/close" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"routeId\": $FETCHED_ID,
    \"savedStock\": [ { \"productId\": $PRODUCT_ID, \"quantity\": 18 } ]
  }")

LIQ_STATUS=$(echo $LIQ_RES | jq -r '.status')
if [ "$LIQ_STATUS" == "PENDING" ]; then
    echo -e "${GREEN}  > Success: Route Closed. Liquidation Status: PENDING${NC}"
else
    echo -e "${RED}  > Fail: Expected PENDING, got $LIQ_STATUS${NC}"
fi

echo -e "\n${BLUE}=== DRIVER VERIFICATION FINISHED ===${NC}"
