make content_server ARGS="cs1 localhost:4567 data1.txt" &

sleep 2

recieved_data=$(make get_client ARGS="localhost:4567 IDS60901")

if [[ "$recieved_data" == *'id --> "IDS60901"'* ]]; then
  echo -e "\033[32mTest 4.1 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 4.1 failed.\033[0m"
fi

make content_server ARGS="cs1 localhost:4567 data2.txt" &

sleep 2

recieved_data_1=$(make get_client ARGS="localhost:4567 IDS60901")

if [[ "$recieved_data_1" == *'id --> "IDS60901"'* ]]; then
  echo -e "\033[32mTest 4.2 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 4.2 failed.\033[0m"
fi