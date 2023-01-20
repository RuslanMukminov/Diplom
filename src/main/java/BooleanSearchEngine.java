import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private static final String FILENAME_STOPWORDS = "stop-ru.txt";
    protected Map<String, List<PageEntry>> mapPageEntry = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File[] fileList = pdfsDir.listFiles();
        for (File pdf : fileList) {
            var doc = new PdfDocument(new PdfReader(pdf));

            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                PdfPage page = doc.getPage(i);
                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");

                Map<String, Integer> freqs = new HashMap<>();
                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }

                for (var word : freqs.keySet()) {
                    var pageEntry = new PageEntry(pdf.getName(), i, freqs.get(word));
                    if (mapPageEntry.containsKey(word)) {
                        List<PageEntry> pageEntryList = new ArrayList<>(mapPageEntry.get(word));
                        pageEntryList.add(pageEntry);
                        mapPageEntry.put(word, pageEntryList);
                    } else {
                        mapPageEntry.put(word, List.of(pageEntry));
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String request) {
        Set<String> stopWordsSet = stopWords();

        List<PageEntry> responseList = new ArrayList<>();

        String[] words = request.split(" ");
        for (String word : words) {
            if (!stopWordsSet.contains(word)) {
                if (mapPageEntry.containsKey(word)) {
                    List<PageEntry> pageEntryList = mapPageEntry.get(word);
                    if (responseList.isEmpty()) {
                        responseList.addAll(pageEntryList);
                        continue;
                    }
                    for (PageEntry pageEntry : pageEntryList) {
                        boolean flagAddPageEntry = true;
                        for (int i = 0; i < responseList.size(); i++) {
                            String pdfNameFromResponse = responseList.get(i).getPdfName();
                            int pageFromResponse = responseList.get(i).getPage();
                            int countFromResponse = responseList.get(i).getCount();

                            if (pageEntry.getPdfName().equals(pdfNameFromResponse)
                                    && (pageEntry.getPage() == pageFromResponse)) {
                                int countSum = countFromResponse + pageEntry.getCount();
                                responseList.set(i, new PageEntry(pdfNameFromResponse,
                                        pageFromResponse, countSum));
                                flagAddPageEntry = false;
                            }
                        }
                        if (flagAddPageEntry) {
                            responseList.add(pageEntry);
                        }
                    }
                }
            }
        }
        if (responseList.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(responseList);
        return responseList;
    }

    private Set<String> stopWords() {
        Set<String> stopWordsSet = new HashSet<>();
        try (var br = new BufferedReader(new FileReader(FILENAME_STOPWORDS))) {
            String stopWord;
            while ((stopWord = br.readLine()) != null) {
                stopWordsSet.add(stopWord);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return stopWordsSet;
    }
}
