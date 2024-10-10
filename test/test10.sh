#!/bin/bash

# Start the Content Server with data1.txt for Test 10
java -cp "lib/jackson/*;out" ContentServer "cs1" "localhost:4567" "data1.txt" &

# Wait for the server to initialize
sleep 2

# Test response time for a valid station ID
echo "Testing response time for a valid station ID"
start_time=$(date +%s%3N)  # Get start time in milliseconds
curl_response=$(curl -s localhost:4567/?stationId=IDS60901)
end_time=$(date +%s%3N)    # Get end time in milliseconds

# Calculate elapsed time
elapsed_time=$((end_time - start_time))

# Display the curl response
echo "$curl_response"
echo ""

# Check if response time is below 200 milliseconds
if [[ $elapsed_time -lt 200 ]]; then
  echo -e "\033[32mTest 10 passed successfully: Response time is ${elapsed_time}ms.\033[0m"
else
  echo -e "\033[31mTest 10 failed: Response time is ${elapsed_time}ms, which exceeds the threshold.\033[0m"
fi
