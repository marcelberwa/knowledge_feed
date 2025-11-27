import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TechNewsScraper - Scrapes titles and snippets from TechCrunch
 * Uses Jsoup library for HTML parsing
 */
public class TechNewsScraper {
    
    private static final String TECH_NEWS_URL = "https://techcrunch.com/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    public static class NewsArticle {
        private String title;
        private String snippet;
        private String url;
        
        public NewsArticle(String title, String snippet, String url) {
            this.title = title;
            this.snippet = snippet;
            this.url = url;
        }
        
        @Override
        public String toString() {
            return "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                   "Title: " + title + "\n" +
                   "Snippet: " + snippet + "\n" +
                   "URL: " + url + "\n";
        }
    }
    
    public static List<NewsArticle> scrapeTechNews(String url) throws IOException {
        List<NewsArticle> articles = new ArrayList<>();
        
        System.out.println("Connecting to: " + url);
        
        // Connect to website and get HTML document
        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get();
        
        System.out.println("Successfully connected to " + doc.title());
        System.out.println("Scraping articles...\n");
        
        // Select article elements (TechCrunch specific selectors)
        // This selects posts/articles from the page
        Elements articleElements = doc.select("article.post-block, div.post-block");
        
        if (articleElements.isEmpty()) {
            // Alternative selector for different layouts
            articleElements = doc.select("article");
        }
        
        for (Element article : articleElements) {
            try {
                // Extract title
                Element titleElement = article.selectFirst("h2 a, h3 a, h2, h3");
                String title = titleElement != null ? titleElement.text() : "No title";
                
                // Extract URL
                Element linkElement = article.selectFirst("a[href]");
                String articleUrl = linkElement != null ? linkElement.attr("abs:href") : "";
                
                // Extract snippet/description
                Element snippetElement = article.selectFirst("p, div.excerpt, div.post-block__content");
                String snippet = snippetElement != null ? snippetElement.text() : "No description available";
                
                // Limit snippet length
                if (snippet.length() > 200) {
                    snippet = snippet.substring(0, 200) + "...";
                }
                
                articles.add(new NewsArticle(title, snippet, articleUrl));
                
                // Limit to first 10 articles
                if (articles.size() >= 10) {
                    break;
                }
            } catch (Exception e) {
                // Skip this article if there's an error
                continue;
            }
        }
        
        return articles;
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("╔════════════════════════════════════════════╗");
            System.out.println("║      Tech News Scraper with Jsoup          ║");
            System.out.println("╚════════════════════════════════════════════╝\n");
            
            List<NewsArticle> articles = scrapeTechNews(TECH_NEWS_URL);
            
            if (articles.isEmpty()) {
                System.out.println("No articles found. The website structure might have changed.");
            } else {
                System.out.println("Found " + articles.size() + " articles:\n");
                
                for (int i = 0; i < articles.size(); i++) {
                    System.out.println("Article #" + (i + 1));
                    System.out.println(articles.get(i));
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error scraping website: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
