import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanSearchEngine implements SearchEngine {
    private static final String FILENAME_STOPWORDS = "stop-ru.txt";
    private static final Set<String> stopWordsSet = new HashSet<>();
    protected Map<String, List<PageEntry>> generalPageEntryMap = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File[] fileList = pdfsDir.listFiles();
        for (File pdf : fileList) {
            Map<String, List<PageEntry>> mapIndexationPdf =
                    new IndexationPdf(pdf).getPageEntryMap();
            for (Map.Entry<String, List<PageEntry>> entry : mapIndexationPdf.entrySet()) {
                generalPageEntryMap.merge(entry.getKey(), entry.getValue(),
                        (val1, val2) -> Stream.concat(val1.stream(), val2.stream())
                                .collect(Collectors.toList()));
            }
        }
        stopWords();
    }

    @Override
    public List<PageEntry> search(String request) {
        List<PageEntry> responseList = new ArrayList<>();
        String[] words = request.split(" ");
        for (String word : words) {
            if (!stopWordsSet.contains(word)) {
                if (generalPageEntryMap.containsKey(word)) {
                    if (responseList.isEmpty()) {
                        responseList.addAll(generalPageEntryMap.get(word));
                        continue;
                    }
                    for (PageEntry pageEntry : generalPageEntryMap.get(word)) {
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

    private void stopWords() {
        try (var br = new BufferedReader(new FileReader(FILENAME_STOPWORDS))) {
            String stopWord;
            while ((stopWord = br.readLine()) != null) {
                stopWordsSet.add(stopWord);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
