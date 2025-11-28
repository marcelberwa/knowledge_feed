import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DailyDigestGUI - Main application for tech news scraping, analysis, and viewing
 */
public class DailyDigestGUI extends JFrame {
    
    private JPanel articlesPanel;
    private JScrollPane scrollPane;
    private JLabel statsLabel;
    private JComboBox<String> dateFilter;
    private JComboBox<String> sortFilter;
    private JButton startImportButton;
    private JButton refreshButton;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private volatile boolean isImporting = false;
    
    private static class Article {
        String title;
        String summary;
        String topics;
        String keyPoints;
        int relevanceScore;
        String url;
        String scrapedDate;
        
        Article(String title, String summary, String topics, String keyPoints, 
                int relevanceScore, String url, String scrapedDate) {
            this.title = title;
            this.summary = summary;
            this.topics = topics;
            this.keyPoints = keyPoints;
            this.relevanceScore = relevanceScore;
            this.url = url;
            this.scrapedDate = scrapedDate;
        }
    }
    
    public DailyDigestGUI() {
        setTitle("Tech News Daily Digest");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        initDatabase();
        loadArticles("today", "relevance");
        
        // Add resize listener for dynamic text wrapping
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshArticleDisplay();
            }
        });
    }
    
    private void initDatabase() {
        try {
            ArticleDatabase.initialize();
            log("Database initialized successfully.");
        } catch (SQLException e) {
            log("ERROR: Failed to initialize database: " + e.getMessage());
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with title, filters, and controls
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(45, 55, 72));
        
        // Title
        JLabel titleLabel = new JLabel("Tech News Daily Digest");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Controls panel (import button + filters)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlsPanel.setBackground(new Color(45, 55, 72));
        
        // Start Import button
        startImportButton = new JButton("Start Import");
        startImportButton.setFont(new Font("Arial", Font.BOLD, 12));
        startImportButton.setBackground(new Color(34, 197, 94));
        startImportButton.setForeground(Color.WHITE);
        startImportButton.setFocusPainted(false);
        startImportButton.addActionListener(e -> startImport());
        
        // Separator
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 25));
        
        // Date filter
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setForeground(Color.WHITE);
        dateFilter = new JComboBox<>(new String[]{"Today", "Last 7 Days", "Last 30 Days", "All Time"});
        dateFilter.addActionListener(e -> filterArticles());
        
        // Sort filter
        JLabel sortLabel = new JLabel("Sort by:");
        sortLabel.setForeground(Color.WHITE);
        sortFilter = new JComboBox<>(new String[]{"Relevance", "Date (Newest)", "Date (Oldest)"});
        sortFilter.addActionListener(e -> filterArticles());
        
        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> filterArticles());
        
        controlsPanel.add(startImportButton);
        controlsPanel.add(Box.createHorizontalStrut(20));
        controlsPanel.add(separator);
        controlsPanel.add(Box.createHorizontalStrut(20));
        controlsPanel.add(dateLabel);
        controlsPanel.add(dateFilter);
        controlsPanel.add(Box.createHorizontalStrut(15));
        controlsPanel.add(sortLabel);
        controlsPanel.add(sortFilter);
        controlsPanel.add(Box.createHorizontalStrut(15));
        controlsPanel.add(refreshButton);
        
        // Stats and progress panel
        JPanel statusPanel = new JPanel(new BorderLayout(10, 5));
        statusPanel.setBackground(new Color(45, 55, 72));
        
        statsLabel = new JLabel("Ready");
        statsLabel.setForeground(Color.WHITE);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        statusPanel.add(statsLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(controlsPanel, BorderLayout.CENTER);
        topPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Main content - split pane with articles and log
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.75);
        
        // Articles panel with scroll
        articlesPanel = new JPanel();
        articlesPanel.setLayout(new BoxLayout(articlesPanel, BoxLayout.Y_AXIS));
        articlesPanel.setBackground(new Color(247, 250, 252));
        
        scrollPane = new JScrollPane(articlesPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Articles"));
        
        // Log panel
        logArea = new JTextArea(6, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Import Log"));
        
        splitPane.setTopComponent(scrollPane);
        splitPane.setBottomComponent(logScrollPane);
        
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void startImport() {
        if (isImporting) {
            log("Import already in progress...");
            return;
        }
        
        isImporting = true;
        startImportButton.setEnabled(false);
        startImportButton.setText("Importing...");
        progressBar.setVisible(true);
        progressBar.setValue(0);
        
        // Run import in background thread
        new Thread(() -> {
            try {
                log("=".repeat(50));
                log("Starting article import...");
                
                // Scrape articles
                log("Connecting to TechCrunch...");
                List<TechNewsScraper.NewsArticle> articles = TechNewsScraper.scrapeTechNews("https://techcrunch.com/");
                
                if (articles.isEmpty()) {
                    log("No articles found. The website structure might have changed.");
                    return;
                }
                
                log("Found " + articles.size() + " articles");
                
                int successCount = 0;
                int analysisCount = 0;
                
                for (int i = 0; i < articles.size(); i++) {
                    TechNewsScraper.NewsArticle article = articles.get(i);
                    int progress = (int) ((i + 1) * 100.0 / articles.size());
                    
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    
                    log("-".repeat(40));
                    log("Article " + (i + 1) + "/" + articles.size() + ": " + article.getTitle());
                    
                    if (!article.getUrl().isEmpty()) {
                        // Check if article already exists
                        try {
                            if (ArticleDatabase.articleExists(article.getUrl())) {
                                log("  [SKIP] Article already in database");
                                continue;
                            }
                        } catch (SQLException e) {
                            log("  [WARN] Could not check for existing article: " + e.getMessage());
                        }
                        
                        // Fetch full article text
                        log("  Fetching article text...");
                        String fullText = ArticleFetcher.fetchArticleText(article.getUrl());
                        article.setArticleText(fullText);
                        
                        // Save to database
                        try {
                            log("  Saving to database...");
                            ArticleDatabase.saveArticle(article);
                            log("  [OK] Article saved");
                            successCount++;
                            
                            // Analyze with LLM
                            log("  Analyzing with LLM...");
                            try {
                                LLMProcessor.ArticleAnalysis analysis = LLMProcessor.analyzeArticle(article);
                                
                                log("  Summary: " + truncate(analysis.getSummary(), 80));
                                log("  Topics: " + String.join(", ", analysis.getTopics()));
                                log("  Relevance: " + analysis.getRelevanceScore() + "/10");
                                
                                ArticleDatabase.saveAnalysis(article.getUrl(), analysis);
                                log("  [OK] Analysis saved");
                                analysisCount++;
                                
                            } catch (Exception e) {
                                log("  [FAIL] LLM analysis failed: " + e.getMessage());
                            }
                            
                        } catch (SQLException e) {
                            log("  [FAIL] Failed to save article: " + e.getMessage());
                        }
                    }
                }
                
                log("=".repeat(50));
                log("Import complete!");
                log("Articles saved: " + successCount + "/" + articles.size());
                log("Articles analyzed: " + analysisCount + "/" + articles.size());
                
                // Refresh article display
                SwingUtilities.invokeLater(() -> filterArticles());
                
            } catch (IOException e) {
                log("ERROR: Failed to scrape articles: " + e.getMessage());
            } finally {
                isImporting = false;
                SwingUtilities.invokeLater(() -> {
                    startImportButton.setEnabled(true);
                    startImportButton.setText("Start Import");
                    progressBar.setVisible(false);
                });
            }
        }).start();
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
    
    private void filterArticles() {
        String dateSelection = ((String) dateFilter.getSelectedItem()).toLowerCase().replace(" ", "_");
        String sortSelection = ((String) sortFilter.getSelectedItem()).toLowerCase();
        loadArticles(dateSelection, sortSelection);
    }
    
    private void refreshArticleDisplay() {
        SwingUtilities.invokeLater(() -> {
            articlesPanel.revalidate();
            articlesPanel.repaint();
        });
    }
    
    private void loadArticles(String dateFilter, String sortBy) {
        articlesPanel.removeAll();
        
        List<Article> articles = fetchArticlesFromDB(dateFilter, sortBy);
        
        if (articles.isEmpty()) {
            JLabel noDataLabel = new JLabel("No articles found. Click 'Start Import' to fetch articles.");
            noDataLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noDataLabel.setForeground(Color.GRAY);
            noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            noDataLabel.setBorder(new EmptyBorder(50, 20, 20, 20));
            articlesPanel.add(noDataLabel);
            statsLabel.setText("No articles");
        } else {
            for (int i = 0; i < articles.size(); i++) {
                articlesPanel.add(createArticlePanel(articles.get(i), i + 1));
                articlesPanel.add(Box.createVerticalStrut(10));
            }
            
            // Update stats
            int totalRelevance = articles.stream().mapToInt(a -> a.relevanceScore).sum();
            double avgRelevance = articles.isEmpty() ? 0 : (double) totalRelevance / articles.size();
            statsLabel.setText(String.format("Total Articles: %d  |  Average Relevance: %.1f/10", 
                    articles.size(), avgRelevance));
        }
        
        articlesPanel.revalidate();
        articlesPanel.repaint();
    }
    
    private List<Article> fetchArticlesFromDB(String dateFilter, String sortBy) {
        List<Article> articles = new ArrayList<>();
        
        try (Connection conn = ArticleDatabase.getConnection()) {
            
            StringBuilder query = new StringBuilder("""
                    SELECT title, summary, topics, key_points, relevance_score, url, 
                           DATE(scraped_date) as scraped_date
                    FROM articles 
                    WHERE summary IS NOT NULL
                    """);
            
            // Add date filter
            switch (dateFilter) {
                case "today":
                    query.append(" AND DATE(scraped_date) = DATE('now', 'localtime')");
                    break;
                case "last_7_days":
                    query.append(" AND DATE(scraped_date) >= DATE('now', '-7 days', 'localtime')");
                    break;
                case "last_30_days":
                    query.append(" AND DATE(scraped_date) >= DATE('now', '-30 days', 'localtime')");
                    break;
                // "all_time" - no filter
            }
            
            // Add sort order
            switch (sortBy) {
                case "relevance":
                    query.append(" ORDER BY relevance_score DESC, id DESC");
                    break;
                case "date (newest)":
                    query.append(" ORDER BY scraped_date DESC, id DESC");
                    break;
                case "date (oldest)":
                    query.append(" ORDER BY scraped_date ASC, id ASC");
                    break;
            }
            
            PreparedStatement pstmt = conn.prepareStatement(query.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                articles.add(new Article(
                    rs.getString("title"),
                    rs.getString("summary"),
                    rs.getString("topics"),
                    rs.getString("key_points"),
                    rs.getInt("relevance_score"),
                    rs.getString("url"),
                    rs.getString("scraped_date")
                ));
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading articles: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        
        return articles;
    }
    
    private JPanel createArticlePanel(Article article, int number) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Header with number and relevance
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel numberLabel = new JLabel("#" + number);
        numberLabel.setFont(new Font("Arial", Font.BOLD, 14));
        numberLabel.setForeground(new Color(100, 116, 139));
        
        JLabel relevanceLabel = new JLabel(getRelevanceBadge(article.relevanceScore));
        relevanceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        relevanceLabel.setOpaque(true);
        relevanceLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
        relevanceLabel.setBackground(getRelevanceColor(article.relevanceScore));
        relevanceLabel.setForeground(Color.WHITE);
        
        headerPanel.add(numberLabel, BorderLayout.WEST);
        headerPanel.add(relevanceLabel, BorderLayout.EAST);
        
        // Title - using JTextArea for dynamic wrapping
        JTextArea titleArea = new JTextArea(article.title);
        titleArea.setWrapStyleWord(true);
        titleArea.setLineWrap(true);
        titleArea.setEditable(false);
        titleArea.setOpaque(false);
        titleArea.setFont(new Font("Arial", Font.BOLD, 16));
        titleArea.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Summary - JTextArea with dynamic wrapping
        if (article.summary != null && !article.summary.isEmpty()) {
            JTextArea summaryArea = new JTextArea(article.summary);
            summaryArea.setWrapStyleWord(true);
            summaryArea.setLineWrap(true);
            summaryArea.setEditable(false);
            summaryArea.setBackground(new Color(241, 245, 249));
            summaryArea.setFont(new Font("Arial", Font.PLAIN, 13));
            summaryArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            contentPanel.add(summaryArea);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        
        // Topics
        if (article.topics != null && !article.topics.isEmpty()) {
            JPanel topicsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            topicsPanel.setBackground(Color.WHITE);
            JLabel topicsLabelTitle = new JLabel("Topics: ");
            topicsLabelTitle.setFont(new Font("Arial", Font.BOLD, 12));
            topicsPanel.add(topicsLabelTitle);
            
            String[] topicArray = article.topics.split(",\\s*");
            for (String topic : topicArray) {
                JLabel topicLabel = new JLabel(topic.trim());
                topicLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                topicLabel.setOpaque(true);
                topicLabel.setBackground(new Color(219, 234, 254));
                topicLabel.setForeground(new Color(30, 64, 175));
                topicLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
                topicsPanel.add(topicLabel);
            }
            contentPanel.add(topicsPanel);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        
        // Key Points - using JTextArea for dynamic wrapping
        if (article.keyPoints != null && !article.keyPoints.isEmpty()) {
            JPanel keyPointsPanel = new JPanel();
            keyPointsPanel.setLayout(new BoxLayout(keyPointsPanel, BoxLayout.Y_AXIS));
            keyPointsPanel.setBackground(Color.WHITE);
            keyPointsPanel.setBorder(new TitledBorder("Key Points"));
            
            String[] points = article.keyPoints.split("\\|");
            for (String point : points) {
                if (!point.trim().isEmpty()) {
                    JTextArea pointArea = new JTextArea("- " + point.trim());
                    pointArea.setWrapStyleWord(true);
                    pointArea.setLineWrap(true);
                    pointArea.setEditable(false);
                    pointArea.setOpaque(false);
                    pointArea.setFont(new Font("Arial", Font.PLAIN, 12));
                    pointArea.setBorder(new EmptyBorder(3, 10, 3, 10));
                    keyPointsPanel.add(pointArea);
                }
            }
            contentPanel.add(keyPointsPanel);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        
        // URL Link and Date
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton urlButton = new JButton("Open Article");
        urlButton.setFont(new Font("Arial", Font.PLAIN, 12));
        urlButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        urlButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI(article.url));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Could not open URL: " + article.url,
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(urlButton);
        
        // Date label
        if (article.scrapedDate != null) {
            JLabel dateLabel = new JLabel("Date: " + article.scrapedDate);
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            dateLabel.setForeground(Color.GRAY);
            buttonPanel.add(Box.createHorizontalStrut(20));
            buttonPanel.add(dateLabel);
        }
        
        contentPanel.add(buttonPanel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(titleArea, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private String getRelevanceBadge(int score) {
        return "Score: " + score + "/10";
    }
    
    private Color getRelevanceColor(int score) {
        if (score >= 8) return new Color(34, 197, 94);  // Green
        if (score >= 6) return new Color(234, 179, 8);  // Yellow
        if (score >= 4) return new Color(249, 115, 22); // Orange
        return new Color(239, 68, 68);                   // Red
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            DailyDigestGUI gui = new DailyDigestGUI();
            gui.setVisible(true);
        });
    }
}
