#!/bin/bash

# Start the Content Server with data1.txt for Test 9
java -cp "lib/jackson/*;out" ContentServer "cs1" "localhost:4567" "data1.txt" &

# Wait for the server to initialize
sleep 2

# Test for no station ID
echo "Testing for no station ID provided"
curl_response=$(curl -s localhost:4567/?stationId=)

# Display the curl response
echo "$curl_response"
echo ""

# Check for expected response (could be a list of all stations or a default message)
if [[ -n "$curl_response" ]]; then
  echo -e "\033[32mTest 9 passed successfully: Received valid response for no station ID.\033[0m"
else
  echo -e "\033[31mTest 9 failed: No response received for no station ID.\033[0m"
fi