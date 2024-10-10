#!/bin/bash

# Start the Content Server with data3.txt for Test 5.1
java -cp "lib/jackson/*;out" ContentServer "cs1" "localhost:4567" "data3.txt" &

# Wait for the server to initialize
sleep 15

# Execute the Client command and capture the output for Test 5.1
recieved_data=$(java -cp "lib/jackson/*;out" Client "localhost:4567" "IDS60907")

# Check if the received data contains the expected ID for Test 5.1
if [[ "$recieved_data" == *'id --> "IDS60907"'* ]]; then
  echo -e "\033[32mTest 5.1 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 5.1 failed.\033[0m"
fi

# Wait before executing the second test
sleep 15

# Execute the Client command again and capture the output for Test 5.2
recieved_data_1=$(java -cp "lib/jackson/*;out" Client "localhost:4567" "IDS60907")

# Check if the received data indicates an error for Test 5.2
if [[ "$recieved_data_1" == *"HTTP/1.1 400"* ]]; then
  echo -e "\033[32mTest 5.2 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 5.2 failed.\033[0m"
fi