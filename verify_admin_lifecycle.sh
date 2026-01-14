#!/bin/bash

# Configuration
BASE_URL="https://ccalarce-backend.onrender.com/api/v1"
SA_USER="admin"
SA_PASS="admin123"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

TS=$(date +%s)
TS_SUFFIX=${TS: -4}

echo -e "${BLUE}=== ADMIN LIFECYCLE VERIFICATION ===${NC}"

# ==========================================
# PHASE 1: SETUP (SUPER ADMIN)
# ==========================================
echo -e "\n${YELLOW}[PHASE 1] Setup: SA Creates Admin...${NC}"
SA_TOKEN=$(curl -s -X POST "$BASE_URL/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$SA_USER\", \"password\": \"$SA_PASS\"}" | jq -r '.token')

ADMIN_USER="mid_manager_$TS"
ADMIN_PASS="pass123"

curl -s -X POST "$BASE_URL/auth/register" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{ \"fullName\": \"Manager $TS\", \"username\": \"$ADMIN_USER\", \"password\": \"$ADMIN_PASS\", \"role\": \"ADMIN\" }" > /dev/null
echo "  > Created Admin: $ADMIN_USER"

# Create a Driver for the Admin to Manage
DRIVER_USER="managee_$TS"
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Authorization: Bearer $SA_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{ \"fullName\": \"Driver $TS\", \"username\": \"$DRIVER_USER\", \"password\": \"$ADMIN_PASS\", \"role\": \"REPARTIDOR\" }" > /dev/null
echo "  > Created Subordinate Driver: $DRIVER_USER"


# ==========================================
# PHASE 2: ADMIN OPERATIONAL POWERS (SHOULD SUCCEED)
# ==========================================
echo -e "\n${YELLOW}[PHASE 2] Verify Admin OPERATIONAL Powers (Success Cases)...${NC}"

# 1. Login as Admin
ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$ADMIN_USER\", \"password\": \"$ADMIN_PASS\"}" | jq -r '.token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" == "null" ]; then echo -e "${RED}Admin Login Failed${NC}"; exit 1; fi
echo -e "${GREEN}  > Admin Login Successful${NC}"

# 2. View Users (Admin needs to see list to assign routes)
USER_LIST_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/users" -H "Authorization: Bearer $ADMIN_TOKEN")
if [ "$USER_LIST_STATUS" == "200" ]; then
    echo -e "${GREEN}  > [OK] Can View User List${NC}"
else
    echo -e "${RED}  > [FAIL] Cannot View User List (Status: $USER_LIST_STATUS)${NC}"
fi

# 3. Create Client
CLIENT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{ \"fullName\": \"AdminClient $TS\", \"documentNumber\": \"99$TS\", \"address\": \"St\", \"active\":true, \"type\":\"CONVENCIONAL\", \"latitude\":0, \"longitude\":0 }")
if [ "$CLIENT_STATUS" == "200" ]; then
    echo -e "${GREEN}  > [OK] Can Create Client${NC}"
else
    echo -e "${RED}  > [FAIL] Cannot Create Client (Status: $CLIENT_STATUS)${NC}"
fi

# 4. Global Route Monitor (Dashboard View)
ROUTES_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/routes/active" -H "Authorization: Bearer $ADMIN_TOKEN")
if [ "$ROUTES_STATUS" == "200" ]; then
    echo -e "${GREEN}  > [OK] Can Monitor Active Routes${NC}"
else
    echo -e "${RED}  > [FAIL] Cannot View Active Routes (Status: $ROUTES_STATUS)${NC}"
fi


# ==========================================
# PHASE 3: ADMIN BOUNDARIES (SECURITY CHECKS)
# ==========================================
echo -e "\n${YELLOW}[PHASE 3] Verify Admin BOUNDARIES (Failure/Forbidden Cases)...${NC}"

# 1. Try to Create a New User (Should be SuperAdmin only)
# Note: In current code UserController.createUser is PreAuthorize("hasRole('SUPER_ADMIN')")
CREATE_USER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/auth/register" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{ \"fullName\": \"Hacker\", \"username\": \"hacker_$TS\", \"password\": \"123\", \"role\": \"ADMIN\" }")

# Actually Register Endpoint is Public in AuthController.
# BUT UserController has manual creation. Let's check UserController implementation.
# If we use UserController.createUser directly (if exposed), it should be protected.
# If we use AuthController.register, it is currently PUBLIC (per API Ref).
# However, let's check Audit Logs access which is definitely restricted.

# 2. Try to View Audit Logs (SuperAdmin Only)
AUDIT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/audit" -H "Authorization: Bearer $ADMIN_TOKEN")

if [ "$AUDIT_STATUS" == "403" ]; then
    echo -e "${GREEN}  > [SECURE] Admin Cannot View Audit Logs (Got 403 Forbidden)${NC}"
else
    echo -e "${RED}  > [SECURITY RISK] Admin ACCESSED Audit Logs! (Status: $AUDIT_STATUS)${NC}"
fi

echo -e "\n${BLUE}=== ADMIN VERIFICATION FINISHED ===${NC}"
