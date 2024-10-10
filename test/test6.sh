#!/bin/bash

# Start the Content Server with data3.txt for Test 6
java -cp "lib/jackson/*;out" ContentServer "cs1" "localhost:4567" "data3.txt" &

# Wait for the server to initialize
sleep 2

# Test for interoperability using CURL for single station ID
echo "Testing for interoperability using CURL for single station ID"
curl_response=$(curl -s localhost:4567/?stationId=IDS60907)

# Display the curl response
echo "$curl_response"
echo ""

# Check if the response contains the expected ID (optional check)
if [[ "$curl_response" == *'id --> "IDS60907"'* ]]; then
  echo -e "\033[32mTest 6 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 6 failed.\033[0m"
fi