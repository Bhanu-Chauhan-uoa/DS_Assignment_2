#!/bin/bash

# Start the Content Server with data1.txt for Test 8
java -cp "lib/jackson/*;out" ContentServer "cs1" "localhost:4567" "data1.txt" &

# Wait for the server to initialize
sleep 2

# Test for an invalid station ID
echo "Testing for invalid station ID"
curl_response=$(curl -s localhost:4567/?stationId=INVALID_ID)

# Display the curl response
echo "$curl_response"
echo ""

# Check for HTTP 400 response
if [[ "$curl_response" == *"HTTP/1.1 400"* ]]; then
  echo -e "\033[32mTest 8 passed successfully: Received HTTP 400 for invalid ID.\033[0m"
else
  echo -e "\033[31mTest 8 failed: Expected HTTP 400 but got different response.\033[0m"
fi