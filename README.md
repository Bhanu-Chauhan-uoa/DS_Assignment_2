<!-- To Compile the files -->
make
<!-- Start Aggregation Server -->
java -cp "lib/jackson/*;out" AggregationServer "4567"
<!-- Start Content Server -->
java -cp "lib/jackson/*;out" ContentServer "CS_Backup" "localhost:4567" "data1.txt" "data2.txt"
<!-- Start Client -->
java -cp "lib/jackson/*;out" Client "localhost:4567"