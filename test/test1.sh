#!/bin/bash

# Start Content Server in the background
java -cp "lib/jackson/*;out" ContentServer "cs1" "localhost:4567" "data1.txt" &

# Wait for the server to start
sleep 5

# Get client data
received_data=$(java -cp "lib/jackson/*;out" Client "localhost:4567")

# Display received data for debugging
echo "Received Data: $received_data"

# Check if the received data contains the expected ID
if [[ "$received_data" == *'id --> "IDS60902"'* ]]; then
  echo -e "\033[32mTest 1 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 1 failed.\033[0m"
fi