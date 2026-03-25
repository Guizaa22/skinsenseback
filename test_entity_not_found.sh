#!/bin/bash

# Test script to verify EntityNotFoundException returns 404 instead of 400

echo "=== Testing EntityNotFoundException Returns 404 ==="
echo ""

# Start the application in background
echo "Starting application..."
mvn spring-boot:run -q &
APP_PID=$!

# Wait for app to start
sleep 10

# Helper function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4

    echo "Testing: $description"

    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method http://localhost:8050$endpoint \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer test-token")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method http://localhost:8050$endpoint \
            -H "Content-Type: application/json" \
            -d "$data" \
            -H "Authorization: Bearer test-token")
    fi

    http_code=$(echo "$response" | tail -1)
    body=$(echo "$response" | head -n -1)

    echo "  Response Code: $http_code"
    if [ "$http_code" = "404" ]; then
        echo "  ✓ PASS: Got expected 404 Not Found"
    else
        echo "  ✗ FAIL: Expected 404, got $http_code"
        echo "  Response: $body"
    fi
    echo ""
}

# Test missing resources (should return 404)
echo "Testing GET endpoints for non-existent resources:"
test_endpoint "GET" "/api/specialties/00000000-0000-0000-0000-000000000000" "" "Get non-existent specialty"
test_endpoint "GET" "/api/services/00000000-0000-0000-0000-000000000000" "" "Get non-existent service"
test_endpoint "GET" "/api/users/00000000-0000-0000-0000-000000000000" "" "Get non-existent user"

# Cleanup
kill $APP_PID

echo "=== Test Complete ==="

