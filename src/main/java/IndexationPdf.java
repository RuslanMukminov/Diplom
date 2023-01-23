import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IndexationPdf {
    private final Map<String, List<PageEntry>> pageEntryMap = new HashMap<>();
    private final File pdf;


    public IndexationPdf(File pdf) throws IOException {
        this.pdf = pdf;
        scanning();
    }

    private void scanning() throws IOException {
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

            for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                pageEntryMap.merge(entry.getKey(),
                        List.of(new PageEntry((pdf.getName()), i, entry.getValue())),
                        (val1, val2) -> Stream.concat(val1.stream(), val2.stream())
                                .collect(Collectors.toList()));
            }
        }
    }

    public Map<String, List<PageEntry>> getPageEntryMap() {
        return pageEntryMap;
    }
}
