import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * ArticleFetcher - Fetches full article text from article URLs
 */
public class ArticleFetcher {
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 10000;
    
    /**
     * Fetches the full article text from the given URL
     * @param articleUrl The URL of the article to fetch
     * @return The full article text, or an error message if fetch fails
     */
    public static String fetchArticleText(String articleUrl) {
        try {
            Document doc = Jsoup.connect(articleUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();
            
            // Extract article body text using common article selectors
            Elements paragraphs = doc.select("article p, div.article-content p, div.entry-content p");
            StringBuilder fullText = new StringBuilder();
            
            for (Element p : paragraphs) {
                String text = p.text();
                if (!text.isEmpty()) {
                    fullText.append(text).append("\n\n");
                }
            }
            
            String text = fullText.toString().trim();
            return text.isEmpty() ? "Article text not available" : text;
            
        } catch (IOException e) {
            System.err.println("Failed to fetch article text from " + articleUrl + ": " + e.getMessage());
            return "Failed to fetch article text";
        }
    }
}
