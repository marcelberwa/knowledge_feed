# Tech News Scraper with Jsoup

A Java application that scrapes titles and snippets from TechCrunch using the Jsoup library.

## Prerequisites

- Java 8 or higher
- Jsoup library

## Setup

### Download Jsoup

Download the Jsoup JAR file from [jsoup.org](https://jsoup.org/download) or use Maven/Gradle:

**Maven:**
```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'org.jsoup:jsoup:1.17.2'
```

**Direct Download:**
```
https://jsoup.org/packages/jsoup-1.17.2.jar
```

## Compilation and Execution

### Option 1: Using Downloaded JAR

1. Download jsoup JAR to your project directory
2. Compile:
```bash
javac -cp jsoup-1.17.2.jar TechNewsScraper.java
```

3. Run:
```bash
java -cp ".;jsoup-1.17.2.jar" TechNewsScraper
```

### Option 2: Using Maven

Create a `pom.xml` file and run:
```bash
mvn compile exec:java -Dexec.mainClass="TechNewsScraper"
```

## Features

- Scrapes latest tech news articles from TechCrunch
- Extracts:
  - Article titles
  - Article snippets/descriptions
  - Article URLs
- Limits snippets to 200 characters for readability
- Fetches up to 10 articles
- Handles errors gracefully

## Customization

You can modify the scraper to work with other news sites by:

1. Changing the `TECH_NEWS_URL` constant
2. Updating the CSS selectors in the `scrapeTechNews()` method to match the target website's HTML structure

## Example Output

```
╔════════════════════════════════════════════╗
║      Tech News Scraper with Jsoup          ║
╚════════════════════════════════════════════╝

Connecting to: https://techcrunch.com/
Successfully connected to TechCrunch
Scraping articles...

Found 10 articles:

Article #1
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Title: Example Tech Article Title
Snippet: This is an example snippet from the article...
URL: https://techcrunch.com/article-url
```

## Legal Notice

When scraping websites:
- Always check the website's `robots.txt` file
- Review the website's Terms of Service
- Respect rate limits and don't overload servers
- Use scraped data responsibly and ethically
- Consider using official APIs when available

## License

This is a demonstration project for educational purposes.
