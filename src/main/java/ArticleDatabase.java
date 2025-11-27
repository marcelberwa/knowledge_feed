import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
                    scraped_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
     * Gets a database connection
     * @return Connection object to the SQLite database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
