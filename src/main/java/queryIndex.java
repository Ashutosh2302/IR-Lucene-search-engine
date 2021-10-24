import java.io.BufferedReader;
import java.io.FileReader;
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
import org.apache.lucene.search.similarities.BM25Similarity;
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
                // English Analyzer gave me best map score
                analyzer= new EnglishAnalyzer();
            }
            else {
                // Standard Analyzer gave me second best map score
                analyzer = new StandardAnalyzer();
            }

            PrintWriter resultWriter = new PrintWriter(resultPath[i], "UTF-8");

            //BM25 Similarity gave me best map score
            isearcher.setSimilarity(new BM25Similarity());

            BufferedReader br = new BufferedReader(new FileReader(String.valueOf("datafiles/cran.qry")));
            MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "author", "bibliography", "words"}, analyzer);

            String line = br.readLine();
            System.out.println("Reading in queries and creating search results.");
            String id = "";
            StringBuilder text = new StringBuilder();
            int j=1;
            while (line != null) {
                if (line.startsWith(".I")) {
                    System.out.println("fetching query no. " + j);
                    id = Integer.toString(j);
                    line = br.readLine();
                }
                if (line.startsWith(".W")) {
                    line = br.readLine();
                    while (line != null && !line.startsWith(".I")) {
                        text.append(line).append(" ");
                        line = br.readLine();
                    }
                }
                text = new StringBuilder(text.toString().trim());
                Query query = parser.parse(QueryParser.escape(text.toString()));

                // Get the set of results from the searcher
                ScoreDoc[] hits = isearcher.search(query, 1400).scoreDocs;
                System.out.println("performing search for query id " + id);
                System.out.println("Documents: " + hits.length);
                for (int k = 0; k < hits.length; k++) {
                    Document hitDoc = isearcher.doc(hits[k].doc);
                    resultWriter.println(id + " 0 " + hitDoc.get("id") + " " + k + " " + hits[k].score  + " XYZ");
                }

                text = new StringBuilder();
                j++;
            }

            System.out.println("Results have been written to the "+ resultPath[i] +" file.");
            ireader.close();
            resultWriter.close();

        }

    }

}
