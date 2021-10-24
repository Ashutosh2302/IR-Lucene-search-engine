import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        String[] indexPaths = {"index_1", "index_2"};

        String docsPath = "datafiles/cran.all.1400";

        final Path docDir = Paths.get(docsPath);

        for (int i=0; i<2; i++) {
            try {
                if(i==0) {
                    System.out.println("using English Analyzer");
                }
                else {
                    System.out.println("using Standard Analyzer");
                }

                System.out.println("Indexing to directory '" + indexPaths[i] + "'...");

                Directory dir = FSDirectory.open(Paths.get(indexPaths[i]));

                Analyzer analyzer = null;
                if(i==0) {
                    analyzer= new EnglishAnalyzer();
                }
                else {
                    analyzer = new StandardAnalyzer();
                }


                IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

                //BM25 Similarity
                iwc.setSimilarity(new BM25Similarity());

                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

                IndexWriter iwriter = new IndexWriter(dir, iwc);
                createIndex(iwriter, docDir);

                //Using writer.forceMerge to maximise search performance.
                iwriter.forceMerge(1);

                iwriter.close();
                System.out.println("Indexing complete!!");

            } catch (IOException e) {
                System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
            }

        }
    }

    static void createIndex(IndexWriter iwriter, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            System.out.println("Indexing documents.....");

            String line = bufferedReader.readLine();


            //looping through whole file and storing datafiles
            while(line != null){
                Document doc = new Document();

                //fetching ID here, as we are sure that id is only one line therefore no need to run a loop
                if(line.startsWith(".I")){
                    doc.add(new StringField("id", line.substring(3), Field.Store.YES));
                    line = bufferedReader.readLine();
                }

                //fetching title and running loop as title could be more than one line
                if (line.startsWith(".T")){
                    line = bufferedReader.readLine();
                    StringBuilder title = new StringBuilder();
                    while(!line.startsWith(".A")){
                        title.append(line).append(" ");
                        line = bufferedReader.readLine();
                    }
                    doc.add(new TextField("title", title.toString(), Field.Store.YES));

                }

                //fetching author and running loop as authors might bemore than one line
                if (line.startsWith(".A")){
                    StringBuilder author = new StringBuilder();
                    line = bufferedReader.readLine();
                    while(!line.startsWith(".B")){
                        author.append(line).append(" ");
                        line = bufferedReader.readLine();
                    }
                    doc.add(new TextField("author", author.toString(), Field.Store.YES));

                }

                //fetching bibliography and running loop as bibliography might be more than one line
                if (line.startsWith(".B")){
                    line = bufferedReader.readLine();
                    StringBuilder bib = new StringBuilder();
                    while(!line.startsWith(".W")){
                        bib.append(line).append(" ");
                        line = bufferedReader.readLine();
                    }
                    doc.add(new StringField("bibliography", bib.toString(), Field.Store.YES));

                }
                //fetching words and running loop as words surely would be more than one line
                if (line.startsWith(".W")){
                    line = bufferedReader.readLine();
                    StringBuilder words = new StringBuilder();
                    while(line != null && !line.startsWith(".I")){
                        words.append(line).append(" ");
                        line = bufferedReader.readLine();
                    }
                    doc.add(new TextField("words", words.toString(), Field.Store.YES));

                }
                iwriter.addDocument(doc);
            }
        }
    }
}

