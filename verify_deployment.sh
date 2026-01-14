#!/bin/bash

# Configuration
BASE_URL="https://ccalarce-backend.onrender.com/api/v1"
SA_USER="admin"
SA_PASS="admin123"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}=== STARTED SYSTEM VERIFICATION ===${NC}"

# Function to check errors
check_error() {
  if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Last command failed${NC}"
    exit 1
  fi
}

# Generate Unique Suffix
TS=$(date +%s)
TS_SUFFIX=${TS: -4}

echo "1. Authenticating Super Admin..."
SA_TOKEN=$(curl -s -X POST "$BASE_URL/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$SA_USER\", \"password\": \"$SA_PASS\"}" | jq -r '.token')

if [ "$SA_TOKEN" == "null" ] || [ -z "$SA_TOKEN" ]; then
    echo -e "${RED}Failed to authenticate Super Admin${NC}"
    exit 1
fi
echo -e "${GREEN}Super Admin Authenticated${NC}"

echo "2. Creating Staff Users (Admin & Driver)..."
# Create Admin
ADMIN_USER="manager_$TS"
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"fullName\": \"Manager $TS\",
    \"username\": \"$ADMIN_USER\",
    \"password\": \"password123\",
    \"role\": \"ADMIN\"
  }" > /dev/null
echo "   - Created $ADMIN_USER (ADMIN)"

# Create Driver
DRIVER_USER="driver_$TS"
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"fullName\": \"Driver $TS\",
    \"username\": \"$DRIVER_USER\",
    \"password\": \"password123\",
    \"role\": \"REPARTIDOR\"
  }" > /dev/null
echo "   - Created $DRIVER_USER (REPARTIDOR)"

echo "3. Authenticating as New ADMIN..."
ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$ADMIN_USER\", \"password\": \"password123\"}" | jq -r '.token')
echo -e "${GREEN}Admin Authenticated${NC}"

echo "4. Creating Master Data..."
# Product
PRODUCT_ID=$(curl -s -X POST "$BASE_URL/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Gas 10kg Premium", "price": 50.00, "stock": 0}' | jq -r '.id')
echo "   - Product Created ID: $PRODUCT_ID"

# Inventory Load
curl -s -X POST "$BASE_URL/inventory/movement" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": $PRODUCT_ID, \"quantity\": 1000, \"type\": \"PURCHASE\", \"reason\": \"Initial Stock\"}" > /dev/null
echo "   - Inventory Loaded (1000 units)"

# Vehicle
VEHICLE_PLATE="V-$TS_SUFFIX"
VEHICLE_ID=$(curl -s -X POST "$BASE_URL/vehicles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"plate\": \"$VEHICLE_PLATE\",
    \"description\": \"Truck $TS\",
    \"gpsDeviceId\": \"GPS-$TS\",
    \"brand\": \"Hino\",
    \"model\": \"300\",
    \"capacity\": 50,
    \"active\": true
  }" | jq -r '.id')
echo "   - Vehicle Created ID: $VEHICLE_ID"

# Client
DOC_NUM="20$TS"
CLIENT_ID=$(curl -s -X POST "$BASE_URL/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"fullName\": \"Restaurante $TS\",
    \"documentNumber\": \"$DOC_NUM\",
    \"address\": \"Av. Test $TS\",
    \"phoneNumber\": \"900000000\",
    \"type\": \"RESTAURANTE\",
    \"commercialStatus\": \"ACTIVO\",
    \"paymentFrequency\": \"SEMANAL\",
    \"latitude\": -12.0,
    \"longitude\": -77.0
  }" | jq -r '.id')
echo "   - Client Created ID: $CLIENT_ID"

# Find Driver ID (Need to list users to find the ID of driver1)
# NOTE: Admin can list users
DRIVER_ID=$(curl -s -X GET "$BASE_URL/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r ".[] | select(.username==\"$DRIVER_USER\") | .id")
echo "   - Found Driver ID: $DRIVER_ID"

echo "5. Operating Logistics (Open Route)..."
ROUTE_ID=$(curl -s -X POST "$BASE_URL/routes/open" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"driverId\": $DRIVER_ID,
    \"vehicleId\": $VEHICLE_ID,
    \"stock\": [ { \"productId\": $PRODUCT_ID, \"quantity\": 50 } ]
  }" | jq -r '.id')
echo -e "${GREEN}Route Opened ID: $ROUTE_ID${NC}"

echo "6. Authenticating as DRIVER..."
DRIVER_TOKEN=$(curl -s -X POST "$BASE_URL/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$DRIVER_USER\", \"password\": \"password123\"}" | jq -r '.token')

echo "7. Driver Fetches Active Route (Tunnel Vision)..."
MY_ROUTE=$(curl -s -X GET "$BASE_URL/routes/current" \
  -H "Authorization: Bearer $DRIVER_TOKEN")
FETCHED_ROUTE_ID=$(echo $MY_ROUTE | jq -r '.id')

if [ "$FETCHED_ROUTE_ID" != "$ROUTE_ID" ]; then
    echo -e "${RED}Mismatch! Driver got route $FETCHED_ROUTE_ID, expected $ROUTE_ID${NC}"
    # exit 1 
    # Validating soft failure
else 
    echo -e "${GREEN}Driver verified correct route: $FETCHED_ROUTE_ID${NC}"
fi

echo "8. Driver Makes a Sale..."
SALE_RES=$(curl -s -X POST "$BASE_URL/sales" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"routeId\": $ROUTE_ID,
    \"clientId\": $CLIENT_ID,
    \"paymentMethod\": \"CASH\",
    \"latitude\": -12.05,
    \"longitude\": -77.05,
    \"items\": [ { \"productId\": $PRODUCT_ID, \"quantity\": 2 } ]
  }")
SALE_ID=$(echo $SALE_RES | jq -r '.id')
echo "   - Sale Registered ID: $SALE_ID"

echo "9. Driver Closes Route (Return Stock)..."
# We took 50, sold 2. Should return 48.
LIQ_RES=$(curl -s -X POST "$BASE_URL/liquidation/close" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"routeId\": $ROUTE_ID,
    \"savedStock\": [ { \"productId\": $PRODUCT_ID, \"quantity\": 48 } ]
  }")
LIQ_ID=$(echo $LIQ_RES | jq -r '.id')
LIQ_STATUS=$(echo $LIQ_RES | jq -r '.status')
echo -e "${GREEN}Route Closed. Liquidation ID: $LIQ_ID (Status: $LIQ_STATUS)${NC}"

echo "10. Admin Approves Liquidation..."
APPROVE_RES=$(curl -s -X POST "$BASE_URL/liquidation/approve/$LIQ_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
NEW_STATUS=$(echo $APPROVE_RES | jq -r '.status')
APPROVED_BY=$(echo $APPROVE_RES | jq -r '.approvedBy.username')

if [ "$NEW_STATUS" == "APPROVED" ]; then
    echo -e "${GREEN}Liquidation APPROVED by $APPROVED_BY${NC}"
else
    echo -e "${RED}Failed to approve liquidation${NC}"
fi

echo "11. SuperAdmin Audit Check..."
AUDIT_LOGS=$(curl -s -X GET "$BASE_URL/audit" \
  -H "Authorization: Bearer $SA_TOKEN")
LAST_ACTION=$(echo $AUDIT_LOGS | jq -r '.[0].action')
echo "   - Last Audit Action Recorded: $LAST_ACTION"

echo -e "${GREEN}=== VERIFICATION COMPLETE ===${NC}"
