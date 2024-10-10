#!/bin/bash

# Start the Content Server with data1.txt for Test 7
java -cp "lib/jackson/*;out" ContentServer "cs1" "localhost:4567" "data1.txt" &

# Wait for the server to initialize
sleep 2

# Test for interoperability using CURL for multiple station IDs
echo "Testing for interoperability using CURL for multiple station ID"
curl_response=$(curl -s localhost:4567/?stationId=)

# Display the curl response
echo "$curl_response"
echo ""

# Check if the response is valid (optional check)
if [[ -n "$curl_response" ]]; then
  echo -e "\033[32mTest 7 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 7 failed: No response received.\033[0m"
fi