# IR-Lucene-search-engine
Run all the following commands from the top level directory

mvn clean

mvn install

mvn exec:java -Dexec.mainClass="createIndex" (This command will trigger two iterations of index creation for english and standard analyzers respectively)

mvn exec:java -Dexec.mainClass="queryIndex" (This command will trigger two iterations of query index for english and standard analyzers respectively)

./trec_eval/trec_eval datafiles/QRelsCorrectedforTRECeval results_1.txt (for english analyzer)

./trec_eval/trec_eval datafiles/QRelsCorrectedforTRECeval results_2.txt (for standard analyzer)
