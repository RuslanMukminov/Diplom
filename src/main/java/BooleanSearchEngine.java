import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
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
    public List<PageEntry> search(String word) {
        if (mapPageEntry.containsKey(word)) {
            List<PageEntry> pageEntryList = mapPageEntry.get(word);
            Collections.sort(pageEntryList);
            return pageEntryList;
        } else {
            return Collections.emptyList();
        }
    }
}
