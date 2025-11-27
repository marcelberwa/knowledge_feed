import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * DailyDigest - Generates a summary of today's analyzed articles
 */
public class DailyDigest {
    
    /**
     * Generates and displays today's tech news digest
     */
    public static void generateDigest() {
        try (Connection conn = ArticleDatabase.getConnection()) {
            
            String query = """
                    SELECT title, summary, topics, key_points, relevance_score, url
                    FROM articles 
                    WHERE DATE(scraped_date) = DATE('now', 'localtime')
                    AND summary IS NOT NULL
                    ORDER BY relevance_score DESC, id DESC
                    """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            int totalRelevance = 0;
            
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║           TECH NEWS DIGEST - " + LocalDate.now() + "              ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");
            
            while (rs.next()) {
                count++;
                String title = rs.getString("title");
                String summary = rs.getString("summary");
                String topics = rs.getString("topics");
                String keyPoints = rs.getString("key_points");
                int relevance = rs.getInt("relevance_score");
                String url = rs.getString("url");
                
                totalRelevance += relevance;
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("Article " + count + " │ Relevance: " + relevance + "/10");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("\n📰 " + title);
                System.out.println("\n📝 Summary:");
                System.out.println("   " + summary);
                
                if (topics != null && !topics.isEmpty()) {
                    System.out.println("\n🏷️  Topics: " + topics);
                }
                
                if (keyPoints != null && !keyPoints.isEmpty()) {
                    System.out.println("\n💡 Key Points:");
                    String[] points = keyPoints.split("\\|");
                    for (String point : points) {
                        if (!point.trim().isEmpty()) {
                            System.out.println("   • " + point.trim());
                        }
                    }
                }
                
                System.out.println("\n🔗 " + url);
                System.out.println();
            }
            
            if (count == 0) {
                System.out.println("No articles have been analyzed yet for today.");
                System.out.println("Run the scraper first to fetch and analyze articles.\n");
            } else {
                double avgRelevance = (double) totalRelevance / count;
                System.out.println("╔════════════════════════════════════════════════════════════╗");
                System.out.println("║                        SUMMARY                             ║");
                System.out.println("╚════════════════════════════════════════════════════════════╝");
                System.out.printf("Total articles analyzed: %d%n", count);
                System.out.printf("Average relevance score: %.1f/10%n", avgRelevance);
                System.out.println();
            }
            
        } catch (SQLException e) {
            System.err.println("Error generating digest: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main entry point for viewing the digest
     */
    public static void main(String[] args) {
        generateDigest();
    }
}
