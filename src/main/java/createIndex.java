import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class createIndex {

    public static void main (String[] args) {

        //index_1 => directory to store English Analyzer's indexes, index_2 => directory to store Standard Analyzer's indexes
        String[] indexPaths = {"index_1", "index_2"};

        //path where cranfield dataset lives
        String datafilesPath = "datafiles/cran.all.1400";
     //   final Path datafilesDir = Paths.get(datafilesPath);

        // running loop just to generate indexes for 2 analyzers
        for (int i=0; i<2; i++) {
            try {
                if(i==0) {
                    System.out.println("using English Analyzer");
                }
                else {
                    System.out.println("using Standard Analyzer");
                }

                System.out.println("Indexing to directory " + indexPaths[i] + "...");

                Directory dir = FSDirectory.open(Paths.get(indexPaths[i]));

                Analyzer analyzer = null;
                if(i==0) {
                    // English Analyzer gave me best map score
                    analyzer= new EnglishAnalyzer();
                }
                else {
                    // Standard Analyzer gave me second best map score
                    analyzer = new StandardAnalyzer();
                }


                IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

                //BM25 Similarity gave me best map score
                iwc.setSimilarity(new BM25Similarity());

                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

                IndexWriter iwriter = new IndexWriter(dir, iwc);
                createStoreIndex(iwriter, datafilesPath);

                //commit everything and close
                iwriter.close();
                System.out.println("Indexing complete!!");

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    static void createStoreIndex(IndexWriter iwriter, String file) throws IOException
    {
            BufferedReader br = new BufferedReader(new FileReader(String.valueOf(file)));
            System.out.println("Indexing documents.....");
            String line = br.readLine();

            // ArrayList of documents
            ArrayList<Document> documents = new ArrayList<Document>();

            //looping through whole file, separating data and creating documents
            while(line != null){
                //create a document
                Document doc = new Document();

                //fetching ID here, as we are sure that id is only one line therefore no need to run a loop
                if(line.startsWith(".I")){
                    String query_id = line.substring(3);
                    doc.add(new StringField("id", query_id, Field.Store.YES));
                    line = br.readLine();
                }

                //fetching title and running loop as title could be more than one line
                if (line.startsWith(".T")){
                    line = br.readLine();
                    StringBuilder title = new StringBuilder();
                    while(!line.startsWith(".A")){
                        title.append(line).append(" ");
                        line = br.readLine();
                    }
                    doc.add(new TextField("title", title.toString(), Field.Store.YES));

                }

                //fetching author and running loop as authors might be more than one line
                if (line.startsWith(".A")){
                    StringBuilder author = new StringBuilder();
                    line = br.readLine();
                    while(!line.startsWith(".B")){
                        author.append(line).append(" ");
                        line = br.readLine();
                    }
                    doc.add(new TextField("author", author.toString(), Field.Store.YES));

                }

                //fetching bibliography and running loop as bibliography might be more than one line
                if (line.startsWith(".B")){
                    line = br.readLine();
                    StringBuilder bib = new StringBuilder();
                    while(!line.startsWith(".W")){
                        bib.append(line).append(" ");
                        line = br.readLine();
                    }
                    doc.add(new StringField("bibliography", bib.toString(), Field.Store.YES));

                }
                //fetching words and running loop as words surely would be more than one line
                if (line.startsWith(".W")){
                    line = br.readLine();
                    StringBuilder words = new StringBuilder();
                    while(line != null && !line.startsWith(".I")){
                        words.append(line).append(" ");
                        line = br.readLine();
                    }
                    doc.add(new TextField("words", words.toString(), Field.Store.YES));

                }
                //adding document to our linked list
                documents.add(doc);

            }
        // Write all the documents in the linked list to the search index
        iwriter.addDocuments(documents);
    }
}

