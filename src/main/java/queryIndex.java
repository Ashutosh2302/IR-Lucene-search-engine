import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.PrintWriter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;

public class queryIndex {

    public static void main (String[] args) throws Exception {

        String[] indexPaths = {"index_1", "index_2"};
        String[] resultPath = {"results_1.txt", "results_2.txt"};

        for(int i=0; i<2 ;i++){
            if(i==0) {
                System.out.println("using English Analyzer");
            }
            else {
                System.out.println("using Standard Analyzer");
            }
            IndexReader ireader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPaths[i])));
            IndexSearcher isearcher = new IndexSearcher(ireader);

            Analyzer analyzer = null;
            if(i==0) {
                analyzer= new EnglishAnalyzer();
            }
            else {
                analyzer = new StandardAnalyzer();
            }


            PrintWriter writer = new PrintWriter(resultPath[i], "UTF-8");

            //BM25 Similarity
            isearcher.setSimilarity(new BM25Similarity());

            String queriesPath = "datafiles/cran.qry";
            BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "author", "bibliography", "words"}, analyzer);

            String line = bufferedReader.readLine();

            System.out.println("Reading in queries and creating search results.");

            String id = "";
            StringBuilder text = new StringBuilder();
            int j=0;
            while (line != null) {
                j++;
                if (line.startsWith(".I")) {
                    System.out.println("fetching query no. " + j);
                    id = Integer.toString(j);
                    line = bufferedReader.readLine();
                }
                if (line.startsWith(".W")) {
                    line = bufferedReader.readLine();
                    while (line != null && !line.startsWith(".I")) {
                        text.append(line).append(" ");
                        line = bufferedReader.readLine();
                    }
                }
                text = new StringBuilder(text.toString().trim());
                Query query = parser.parse(QueryParser.escape(text.toString()));
                ScoreDoc[] hits = isearcher.search(query, 1400).scoreDocs;
                System.out.println("performing search for query id " + id);
                System.out.println("Documents: " + hits.length);
                for (int k = 0; k < hits.length; k++) {
                    Document hitDoc = isearcher.doc(hits[k].doc);
                    writer.println(id + " 0 " + hitDoc.get("id") + " " + k + " " + hits[k].score  + " XYZ");
                }

                text = new StringBuilder();
            }

            System.out.println("Results have been written to the "+ resultPath[i] +" file.");
            ireader.close();
            writer.close();

        }

    }

}
