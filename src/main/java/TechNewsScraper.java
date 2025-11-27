import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TechNewsScraper - Scrapes titles and snippets from TechCrunch
 * Uses Jsoup library for HTML parsing
 */
public class TechNewsScraper {
    
    private static final String TECH_NEWS_URL = "https://techcrunch.com/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 10000;
    
    /**
     * NewsArticle - Represents a scraped news article
     */
    public static class NewsArticle {
        private final String title;
        private final String snippet;
        private final String url;
        private String articleText;
        
        public NewsArticle(String title, String snippet, String url) {
            this.title = title;
            this.snippet = snippet;
            this.url = url;
            this.articleText = "";
        }
        
        public String getTitle() { 
            return title; 
        }
        
        public String getSnippet() { 
            return snippet; 
        }
        
        public String getUrl() { 
            return url; 
        }
        
        public String getArticleText() { 
            return articleText; 
        }
        
        public void setArticleText(String text) { 
            this.articleText = text; 
        }
        
        @Override
        public String toString() {
            return "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                   "Title: " + title + "\n" +
                   "Snippet: " + snippet + "\n" +
                   "URL: " + url + "\n";
        }
    }
    
    /**
     * Scrapes tech news articles from the given URL
     * @param url The URL to scrape
     * @return List of NewsArticle objects
     * @throws IOException if connection fails
     */
    public static List<NewsArticle> scrapeTechNews(String url) throws IOException {
        List<NewsArticle> articles = new ArrayList<>();
        
        System.out.println("Connecting to: " + url);
        
        // Connect to website and get HTML document
        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .get();
        
        System.out.println("Successfully connected to " + doc.title());
        System.out.println("Scraping articles...\n");
        
        // Select article elements (TechCrunch specific selectors)
        // Updated selector for loop-card structure
        Elements articleElements = doc.select("div.loop-card");
        
        for (Element article : articleElements) {
            try {
                // Extract title from h3.loop-card__title a
                Element titleElement = article.selectFirst("h3.loop-card__title a");
                if (titleElement == null) {
                    // Fallback to generic selectors
                    titleElement = article.selectFirst("h2 a, h3 a, h2, h3");
                }
                String title = titleElement != null ? titleElement.text() : "No title";
                
                // Extract URL from title link
                Element linkElement = article.selectFirst("h3.loop-card__title a");
                if (linkElement == null) {
                    // Fallback to any link
                    linkElement = article.selectFirst("a[href]");
                }
                String articleUrl = linkElement != null ? linkElement.attr("abs:href") : "";
                
                // Extract category/snippet from loop-card__cat or meta
                Element categoryElement = article.selectFirst("a.loop-card__cat");
                String snippet = categoryElement != null ? categoryElement.text() : "";
                
                // If no category, try to get excerpt from paragraphs
                if (snippet.isEmpty()) {
                    Element snippetElement = article.selectFirst("p, div.excerpt, div.post-block__content");
                    snippet = snippetElement != null ? snippetElement.text() : "No description available";
                }
                
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
    
    /**
     * Processes articles: fetches full text and saves to database
     * @param articles List of articles to process
     */
    private static void processArticles(List<NewsArticle> articles) {
        for (int i = 0; i < articles.size(); i++) {
            NewsArticle article = articles.get(i);
            System.out.println("\nArticle #" + (i + 1));
            System.out.println(article);
            
            // Fetch full article text
            if (!article.getUrl().isEmpty()) {
                System.out.println("Fetching full article text...");
                String fullText = ArticleFetcher.fetchArticleText(article.getUrl());
                article.setArticleText(fullText);
                
                // Save to database
                try {
                    ArticleDatabase.saveArticle(article);
                    System.out.println("✓ Saved to database");
                } catch (SQLException e) {
                    System.err.println("✗ Failed to save article: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        try {
            System.out.println("╔════════════════════════════════════════════╗");
            System.out.println("║      Tech News Scraper with Jsoup          ║");
            System.out.println("╚════════════════════════════════════════════╝\n");
            
            // Initialize database
            ArticleDatabase.initialize();
            
            // Scrape articles
            List<NewsArticle> articles = scrapeTechNews(TECH_NEWS_URL);
            
            if (articles.isEmpty()) {
                System.out.println("No articles found. The website structure might have changed.");
            } else {
                System.out.println("Found " + articles.size() + " articles\n");
                processArticles(articles);
                
                System.out.println("\n╔════════════════════════════════════════════╗");
                System.out.println("║ All articles saved to tech_news.db         ║");
                System.out.println("╚════════════════════════════════════════════╝");
            }
            
        } catch (IOException e) {
            System.err.println("Error scraping website: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
