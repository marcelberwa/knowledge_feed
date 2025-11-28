import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ArticleDatabase - Handles all database operations for tech news articles
 * Uses SQLite for storage
 */
public class ArticleDatabase {
    
    private static final String DB_URL = "jdbc:sqlite:tech_news.db";
    
    /**
     * Initializes the database and creates the articles table if it doesn't exist
     * @throws SQLException if database initialization fails
     */
    public static void initialize() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS articles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    url TEXT UNIQUE NOT NULL,
                    snippet TEXT,
                    article_text TEXT,
                    summary TEXT,
                    topics TEXT,
                    key_points TEXT,
                    relevance_score INTEGER,
                    scraped_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    analyzed_date TIMESTAMP
                )
                """;
            
            stmt.execute(createTableSQL);
            System.out.println("Database initialized successfully.");
        }
    }
    
    /**
     * Saves an article to the database
     * @param article The NewsArticle to save
     * @throws SQLException if save operation fails
     */
    public static void saveArticle(TechNewsScraper.NewsArticle article) throws SQLException {
        String insertSQL = "INSERT OR REPLACE INTO articles (title, url, snippet, article_text) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            
            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getUrl());
            pstmt.setString(3, article.getSnippet());
            pstmt.setString(4, article.getArticleText());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Saves article analysis results to the database
     * @param url The article URL
     * @param analysis The LLM analysis results
     * @throws SQLException if update operation fails
     */
    public static void saveAnalysis(String url, LLMProcessor.ArticleAnalysis analysis) throws SQLException {
        String updateSQL = """
                UPDATE articles 
                SET summary = ?, topics = ?, key_points = ?, relevance_score = ?, analyzed_date = CURRENT_TIMESTAMP 
                WHERE url = ?
                """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            
            pstmt.setString(1, analysis.getSummary());
            pstmt.setString(2, String.join(", ", analysis.getTopics()));
            pstmt.setString(3, String.join(" | ", analysis.getKeyPoints()));
            pstmt.setInt(4, analysis.getRelevanceScore());
            pstmt.setString(5, url);
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Checks if an article with the given URL already exists in the database
     * @param url The article URL to check
     * @return true if article exists, false otherwise
     * @throws SQLException if query fails
     */
    public static boolean articleExists(String url) throws SQLException {
        String querySQL = "SELECT COUNT(*) FROM articles WHERE url = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            
            pstmt.setString(1, url);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Deletes all articles from the database.
     * Use with caution - this removes all rows from the articles table.
     * @throws SQLException if the delete operation fails
     */
    public static void deleteAllArticles() throws SQLException {
        String deleteSQL = "DELETE FROM articles";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Gets a database connection
     * @return Connection object to the SQLite database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
