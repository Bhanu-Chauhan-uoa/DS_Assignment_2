recieved_data=$(make get_client ARGS="localhost:4567 IDS60901")

if [[ "$recieved_data" == *"HTTP/1.1 400"* ]]; then
  echo -e "\033[32mTest 3.1 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 3.1 failed.\033[0m"
fi
make content_server ARGS="cs1 localhost:4567 data1.txt data3.txt" &

sleep 5

recieved_data_1=$(make get_client ARGS="localhost:4567 IDS60901")

if [[ "$recieved_data_1" == *'id --> "IDS60901"'* ]]; then
  echo -e "\033[32mTest 3.2 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 3.2 failed.\033[0m"
fi