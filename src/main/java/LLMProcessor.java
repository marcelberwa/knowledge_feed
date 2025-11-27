import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * LLMProcessor - Handles communication with LM Studio for article analysis
 */
public class LLMProcessor {
    
    private static final String LM_STUDIO_URL = "http://192.168.0.227:1234/v1/chat/completions";
    private static final int TIMEOUT_SECONDS = 60;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();
    private static final Gson gson = new Gson();
    
    /**
     * ArticleAnalysis - Holds the results of LLM analysis
     */
    public static class ArticleAnalysis {
        private String summary;
        private String[] topics;
        private String[] keyPoints;
        private int relevanceScore;
        
        public ArticleAnalysis(String summary, String[] topics, String[] keyPoints, int relevanceScore) {
            this.summary = summary;
            this.topics = topics;
            this.keyPoints = keyPoints;
            this.relevanceScore = relevanceScore;
        }
        
        public String getSummary() { return summary; }
        public String[] getTopics() { return topics; }
        public String[] getKeyPoints() { return keyPoints; }
        public int getRelevanceScore() { return relevanceScore; }
    }
    
    /**
     * Analyzes an article using the LLM
     * @param article The article to analyze
     * @return ArticleAnalysis with summary, topics, key points, and relevance score
     * @throws IOException if the API call fails
     * @throws InterruptedException if the request is interrupted
     */
    public static ArticleAnalysis analyzeArticle(TechNewsScraper.NewsArticle article) 
            throws IOException, InterruptedException {
        
        String prompt = buildAnalysisPrompt(article);
        String response = callLLM(prompt);
        return parseAnalysisResponse(response);
    }
    
    /**
     * Builds the prompt for article analysis
     */
    private static String buildAnalysisPrompt(TechNewsScraper.NewsArticle article) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following tech news article and provide:\n");
        prompt.append("1. A concise 2-3 sentence summary\n");
        prompt.append("2. Main topics/technologies mentioned (comma-separated)\n");
        prompt.append("3. Key takeaways (3-5 bullet points)\n");
        prompt.append("4. Relevance score (1-10, where 10 is highly significant tech news)\n\n");
        prompt.append("Article Title: ").append(article.getTitle()).append("\n\n");
        
        String articleText = article.getArticleText();
        if (articleText != null && !articleText.isEmpty() && 
            !articleText.equals("Article text not available") && 
            !articleText.equals("Failed to fetch article text")) {
            // Limit text length to avoid token limits
            int maxLength = 4000;
            if (articleText.length() > maxLength) {
                articleText = articleText.substring(0, maxLength) + "...";
            }
            prompt.append("Article Text:\n").append(articleText);
        } else {
            prompt.append("Article Snippet: ").append(article.getSnippet());
        }
        
        prompt.append("\n\nProvide your analysis in this exact format:\n");
        prompt.append("SUMMARY: [your summary]\n");
        prompt.append("TOPICS: [topic1, topic2, topic3]\n");
        prompt.append("KEY_POINTS:\n- [point 1]\n- [point 2]\n- [point 3]\n");
        prompt.append("RELEVANCE: [score]");
        
        return prompt.toString();
    }
    
    /**
     * Calls the LM Studio API
     */
    private static String callLLM(String userPrompt) throws IOException, InterruptedException {
        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "local-model");
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 1000);
        
        JsonArray messages = new JsonArray();
        
        // System message
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", 
            "You are a tech news analyst. Provide clear, concise analysis of technology articles. " +
            "Focus on accuracy and relevance. Follow the exact format requested.");
        messages.add(systemMessage);
        
        // User message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messages.add(userMessage);
        
        requestBody.add("messages", messages);
        
        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LM_STUDIO_URL))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        // Send request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("LLM API returned status code: " + response.statusCode() + 
                                " Body: " + response.body());
        }
        
        // Parse response
        JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
        JsonArray choices = responseJson.getAsJsonArray("choices");
        if (choices != null && choices.size() > 0) {
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            return message.get("content").getAsString();
        }
        
        throw new IOException("No response from LLM");
    }
    
    /**
     * Parses the LLM response into structured data
     */
    private static ArticleAnalysis parseAnalysisResponse(String response) {
        String summary = "";
        String[] topics = new String[0];
        String[] keyPoints = new String[0];
        int relevanceScore = 5;
        
        try {
            String[] lines = response.split("\n");
            StringBuilder keyPointsBuilder = new StringBuilder();
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("SUMMARY:")) {
                    summary = line.substring("SUMMARY:".length()).trim();
                } else if (line.startsWith("TOPICS:")) {
                    String topicsStr = line.substring("TOPICS:".length()).trim();
                    topics = topicsStr.split(",\\s*");
                } else if (line.startsWith("-") || line.startsWith("•")) {
                    keyPointsBuilder.append(line.substring(1).trim()).append("|");
                } else if (line.startsWith("RELEVANCE:")) {
                    String scoreStr = line.substring("RELEVANCE:".length()).trim();
                    try {
                        relevanceScore = Integer.parseInt(scoreStr.replaceAll("[^0-9]", ""));
                        if (relevanceScore < 1) relevanceScore = 1;
                        if (relevanceScore > 10) relevanceScore = 10;
                    } catch (NumberFormatException e) {
                        relevanceScore = 5;
                    }
                }
            }
            
            if (keyPointsBuilder.length() > 0) {
                keyPoints = keyPointsBuilder.toString().split("\\|");
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing LLM response: " + e.getMessage());
            summary = "Analysis parsing failed";
        }
        
        return new ArticleAnalysis(summary, topics, keyPoints, relevanceScore);
    }
}
