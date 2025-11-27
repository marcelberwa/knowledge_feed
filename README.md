# Tech News Scraper with AI Analysis

A Java 21 application that scrapes tech news from TechCrunch, stores articles in SQLite, and uses a local LLM (via LM Studio) to analyze and summarize the content.

## Features

### 🔍 Web Scraping
- Scrapes latest tech news articles from TechCrunch
- Extracts titles, snippets, URLs, and full article text
- Handles modern `loop-card` HTML structure
- Fetches up to 10 articles per run

### 💾 Database Storage
- SQLite database for persistent storage
- Stores article metadata and full text
- Tracks scraping and analysis timestamps
- Prevents duplicate articles (by URL)

### 🤖 AI-Powered Analysis
- Integrates with LM Studio for local LLM processing
- Generates concise 2-3 sentence summaries
- Extracts main topics and technologies
- Identifies key takeaways (3-5 bullet points)
- Assigns relevance scores (1-10)
- All analysis stored in database

### 📊 Daily Digest
- View analyzed articles sorted by relevance
- Summary statistics and insights
- Formatted output for easy reading

## Prerequisites

- **Java 21** (LTS)
- **Maven** 3.8+
- **LM Studio** running locally with a loaded model
  - Default endpoint: `http://192.168.0.227:1234/v1/chat/completions`

## Project Structure

```
src/main/java/
├── TechNewsScraper.java    # Main scraper and orchestration
├── ArticleDatabase.java     # Database operations
├── ArticleFetcher.java      # Full article text fetching
├── LLMProcessor.java        # LLM integration and analysis
└── DailyDigest.java         # Daily summary viewer
```

## Setup

1. **Ensure Java 21 is installed:**
```bash
java -version
```

2. **Start LM Studio:**
   - Load your preferred model
   - Start the local server (default: port 1234)
   - Note the IP address and port

3. **Configure LLM endpoint** (if different from default):
   Edit `LLMProcessor.java`:
   ```java
   private static final String LM_STUDIO_URL = "http://YOUR_IP:PORT/v1/chat/completions";
   ```

## Usage

### Scrape and Analyze Articles

```bash
mvn compile exec:java -Dexec.mainClass="TechNewsScraper"
```

This will:
1. Scrape articles from TechCrunch
2. Fetch full article text
3. Save to database
4. Analyze each article with LLM
5. Store analysis results

### View Daily Digest

```bash
mvn compile exec:java -Dexec.mainClass="DailyDigest"
```

Displays:
- All analyzed articles from today
- Summaries and key points
- Topics and relevance scores
- Overall statistics

## Database Schema

```sql
CREATE TABLE articles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    url TEXT UNIQUE NOT NULL,
    snippet TEXT,
    article_text TEXT,
    summary TEXT,              -- LLM-generated summary
    topics TEXT,               -- Comma-separated topics
    key_points TEXT,           -- Pipe-separated key points
    relevance_score INTEGER,   -- 1-10 relevance rating
    scraped_date TIMESTAMP,
    analyzed_date TIMESTAMP
)
```

## Example Output

### Scraping Process
```
╔════════════════════════════════════════════╗
║      Tech News Scraper with Jsoup          ║
╚════════════════════════════════════════════╝

Database initialized successfully.
Found 10 articles

============================================================
Article #1 of 10
============================================================
Title: Example Tech Article Title
URL: https://techcrunch.com/article-url

[1/3] Fetching full article text...
[2/3] Saving to database...
✓ Article saved
[3/3] Analyzing with LLM...

--- LLM Analysis ---
Summary: This article discusses...
Topics: AI, Machine Learning, Cloud Computing
Key Points:
  • First key takeaway
  • Second important point
  • Third insight
Relevance Score: 8/10
✓ Analysis saved
```

### Daily Digest
```
╔════════════════════════════════════════════════════════════╗
║           TECH NEWS DIGEST - 2025-11-27                    ║
╚════════════════════════════════════════════════════════════╝

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Article 1 │ Relevance: 9/10
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📰 Major AI Breakthrough Announced
📝 Summary: Company X released a new AI model that...
🏷️  Topics: AI, Deep Learning, Natural Language Processing
💡 Key Points:
   • 40% improvement in accuracy
   • Open source release planned
   • Available for commercial use
🔗 https://techcrunch.com/...
```

## Customization

### Change Target Website
Modify `TechNewsScraper.java`:
```java
private static final String TECH_NEWS_URL = "https://your-site.com/";
```
Update CSS selectors in `scrapeTechNews()` method.

### Adjust LLM Analysis Prompt
Modify `LLMProcessor.buildAnalysisPrompt()` to customize:
- Summary length
- Number of key points
- Additional analysis fields

### Configure LLM Parameters
Edit `LLMProcessor.callLLM()`:
```java
requestBody.addProperty("temperature", 0.7);  // Creativity (0.0-1.0)
requestBody.addProperty("max_tokens", 1000);  // Response length
```

## Dependencies

- **Jsoup 1.17.2** - HTML parsing
- **SQLite JDBC 3.47.1.0** - Database
- **Gson 2.11.0** - JSON handling
- **Java 21 HTTP Client** - LLM API calls

## Legal Notice

When scraping websites:
- ✅ Check `robots.txt` compliance
- ✅ Review Terms of Service
- ✅ Respect rate limits
- ✅ Use data responsibly
- ✅ Consider official APIs when available

## License

Educational and demonstration purposes.

## Troubleshooting

**LLM connection fails:**
- Verify LM Studio is running
- Check IP address and port
- Ensure model is loaded

**No articles found:**
- Website structure may have changed
- Update CSS selectors in scraper

**Database errors:**
- Check file permissions
- Ensure SQLite driver is loaded
- Delete `tech_news.db` to reset
