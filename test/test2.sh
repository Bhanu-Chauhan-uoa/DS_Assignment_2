make content_server ARGS="cs1 localhost:4567 data1.txt" &

sleep 2

recieved_data=$(make get_client ARGS="localhost:4567 IDS6090111")

if [[ "$recieved_data" == *"HTTP/1.1 400"* ]]; then
  echo -e "\033[32mTest 2 passed successfully.\033[0m"
else
  echo -e "\033[31mTest 2 failed.\033[0m"
fi