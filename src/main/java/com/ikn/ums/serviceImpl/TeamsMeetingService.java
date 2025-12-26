package com.ikn.ums.serviceImpl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.azure.core.credential.AccessToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.ikn.ums.util.GraphClientConfig;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
@Slf4j
@Service
public class TeamsMeetingService {
	
	  @Autowired
	  private GraphClientConfig graphClient;
	 
	  private AccessToken acToken = new AccessToken(this.accessToken, OffsetDateTime.now());

	  private String accessToken = null;
	  
	  private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
	  
      private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
     
      private final OkHttpClient client = new OkHttpClient();
	  
	  @Autowired
	  @Qualifier("externalRestTemplate")
	  private RestTemplate restTemplate;
	  
	  @Autowired
	  private CopilotService copilotService;

	    /**
	     * Fetch Copilot summary for a meeting
	     * 
	     */
	  @PostConstruct
	  public void init() {
	      try {
	          AccessToken token = graphClient.initializeMicrosoftGraph();
	          this.accessToken = token.getToken();
	      } catch (Exception e) {
	          throw new RuntimeException("Failed to initialize TeamsMeetingService", e);
	      }
	  }
	    
	    public JsonNode getCopilotSummary(String meetingId) throws Exception {
	        String url = "https://graph.microsoft.com/beta/communications/onlineMeetings/"
	                + meetingId + "/copilotSummary";
	        // 2️⃣ Prepare headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Authorization", "Bearer " + accessToken);
	        headers.add("Content-Type", "application/json");
	        
	        // 3️⃣ Create HTTP entity
	        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

	        // 4️⃣ Use RestTemplate
	        RestTemplate restTemplate = new RestTemplate();

	        // 5️⃣ Call endpoint
	        ResponseEntity<JsonNode> response = restTemplate.exchange(
	                url,
	                HttpMethod.GET,
	                httpEntity,
	                new ParameterizedTypeReference<JsonNode>() {}
	        );

	        return response.getBody();
	    }

	    /**
	     * Fetch the transcript metadata (to get download URL)
	     */
	    public Object getTranscriptsMetadata(String meetingId) throws Exception {
	    	log.info("the method is executing....");
	    	//String userEmail = "bharat@ikcontech.com";
	    	String userEmail = "36f3cc24-2a46-4da3-a1f6-9f83e7bdd465";
	    	String url = "https://graph.microsoft.com/v1.0/users/" 
	    	           + userEmail 
	    	           + "/onlineMeetings/" 
	    	           + meetingId 
	    	           + "/transcripts";

	        AccessToken token = graphClient.initializeMicrosoftGraph();
	          this.accessToken = token.getToken();
            
	        // 2️⃣ Prepare headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Authorization", "Bearer "+accessToken);
	        headers.add("Content-Type", "application/json");
	        
	        // 3️⃣ Create HTTP entity
	        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

	        // 4️⃣ Use RestTemplate
	        restTemplate = new RestTemplate();

	        // 5️⃣ Call endpoint
	        ResponseEntity<JsonNode> response = restTemplate.exchange(
	                url.toString(),
	                HttpMethod.GET,
	                httpEntity,
	                new ParameterizedTypeReference<JsonNode>() {}
	        );
	       
	        String transcriptContentUrl = response.getBody()
	                .get("value")
	                .get(0)
	                .get("transcriptContentUrl")
	                .asText();
	        String transcriptText = getTranscriptContent(accessToken, transcriptContentUrl);
	        log.info("the transcript content"+ transcriptText);
	        String transcript = cleanTranscript(transcriptText);
	        //extractActionItems(transcriptText,"Data");
	        //sendToCopilot(transcriptText,this.accessToken);

	        return transcriptText;
	    }

//	    /**
//	     * Download the transcript file (VTT or DOCX)
//	     */
//	    public InputStream downloadTranscriptFile(String downloadUrl) throws Exception {
//	        return graphClient
//	                .customRequest(downloadUrl, InputStream.class)
//	                .buildRequest()
//	                .get();
//	    }

	    /**
	     * Fetch the speaker-wise transcript
	     */
	    public JsonNode getSpeakerTranscript(String meetingId, String transcriptId) throws Exception {
	    	 
	        String url = "https://graph.microsoft.com/v1.0/communications/onlineMeetings/"
	                + meetingId + "/transcripts/" + transcriptId + "/content";

	        // 2️⃣ Prepare headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Authorization", "Bearer " + accessToken);
	        headers.add("Content-Type", "application/json");
	        
	        // 3️⃣ Create HTTP entity
	        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

	        // 4️⃣ Use RestTemplate
	        RestTemplate restTemplate = new RestTemplate();

	        // 5️⃣ Call endpoint
	        ResponseEntity<JsonNode> response = restTemplate.exchange(
	                url,
	                HttpMethod.GET,
	                httpEntity,
	                new ParameterizedTypeReference<JsonNode>() {}
	        );

	        return response.getBody();
	    }
	    
	 // Fetch Meeting AI Insights from Graph Beta
	    public JsonObject getMeetingInsights(String meetingId) {
	        try {
	            String url = "https://graph.microsoft.com/beta/communications/callRecords/"
	                    + meetingId + "/aiInsights";

	            // Request JSON directly
	            // 2️⃣ Prepare headers
		        HttpHeaders headers = new HttpHeaders();
		        headers.add("Authorization", "Bearer " + accessToken);
		        headers.add("Content-Type", "application/json");
		        
		        // 3️⃣ Create HTTP entity
		        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

		        // 4️⃣ Use RestTemplate
		        RestTemplate restTemplate = new RestTemplate();

		        // 5️⃣ Call endpoint
		        ResponseEntity<JsonNode> response = restTemplate.exchange(
		                url,
		                HttpMethod.GET,
		                httpEntity,
		                new ParameterizedTypeReference<JsonNode>() {}
		        );

		        

	            // Convert JsonNode → JsonObject (Gson)
	            //return JsonParser.parseString(response.toString()).getAsJsonObject();
	            return com.google.gson.JsonParser
	                    .parseString(response.getBody().toString())
	                    .getAsJsonObject();

	        } catch (Exception e) {
	            throw new RuntimeException("Failed to fetch AI insights: " + e.getMessage(), e);
	        }
	    }
	
	    /**
	     * Fetch the transcript metadata (to get download URL)
	     */
	    public Object getMeeting(String meetingId) throws Exception {
	    	log.info("the method is executing....");
	    	String userEmail = "36f3cc24-2a46-4da3-a1f6-9f83e7bdd465";
	    	//String userEmail = "bharat@ikcontech.com";
	    	//String url = "https://graph.microsoft.com/v1.0/users/" 
	    	//           + userEmail 
	    	//           + "/onlineMeetings/" 
	    	//           + meetingId;
	    	String url = "https://graph.microsoft.com/v1.0/users/"
	    	        + "bharat@ikcontech.com"
	    	        + "/onlineMeetings?$filter=joinWebUrl eq '%s'";
           log.info("the url is"+ url);
	        AccessToken token = graphClient.initializeMicrosoftGraph();
	          this.accessToken = token.getToken();
            
	        // 2️⃣ Prepare headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Authorization", "Bearer "+this.accessToken);
	        headers.add("Content-Type", "application/json");
	        
	        // 3️⃣ Create HTTP entity
	        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

	        // 4️⃣ Use RestTemplate
	        restTemplate = new RestTemplate();

	        // 5️⃣ Call endpoint
	        ResponseEntity<Object> response = null;
	        try {
	        	log.info("control is here");
	        	response = restTemplate.exchange(
	 	                url.toString(),
	 	                HttpMethod.GET,
	 	                httpEntity,
	 	                new ParameterizedTypeReference<Object>() {}
	 	        );
	        	
	        }catch(Exception e) {
	        	 
	        	log.error("error is"+e);
	        }
	       

	        return response.getBody();
	    }
	    
	    /**
	     * 
	     * @param userEmail1
	     * @param meetingId
	     * @return
	     * @throws Exception 
	     * Working code to fecth the meeting Data
	     */
	    
	    public JsonNode getMeetingData(String userEmail1, String meetingId) throws Exception {

	    	//String userEmail = "36f3cc24-2a46-4da3-a1f6-9f83e7bdd465";
	    	String userEmail = getUserIdFromEmail(userEmail1);
	    	log.info(" the teams user Id is"+ userEmail);
	    	//String userEmail = "50621729-1bd1-4c8f-8b73-f54b2609c9ab";
	        String url = "https://graph.microsoft.com/v1.0/users/"
	                +userEmail
	                +"/onlineMeetings/"+meetingId.trim();
	        AccessToken token = graphClient.initializeMicrosoftGraph();
	          this.accessToken = token.getToken();
            log.info("the access Token is "+ this.accessToken);
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + accessToken);
	        headers.set("Content-Type", "application/json");

	        HttpEntity<String> entity = new HttpEntity<>(headers);

	        try {
	        	log.info("entered the method ");
	            return restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class).getBody();
	        } catch (HttpClientErrorException e) {
	            throw new RuntimeException("Graph API Error: " + e.getResponseBodyAsString());
	        }
	    }

	    
	    public String getUserIdFromEmail(String userEmail) throws Exception {

	        String url = "https://graph.microsoft.com/v1.0/users/" + userEmail;

	        AccessToken token = graphClient.initializeMicrosoftGraph();
	        String accessToken = token.getToken();

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + accessToken);

	        HttpEntity<String> entity = new HttpEntity<>(headers);

	        JsonNode response = restTemplate.exchange(
	                url, HttpMethod.GET, entity, JsonNode.class).getBody();

	        return response.get("id").asText();  // GUID


      }
	   
	    public String getTranscriptContent(String accessToken, String transcriptContentUrl) throws Exception {

	        RestTemplate restTemplate = new RestTemplate();

	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Authorization", "Bearer " + accessToken);
	        headers.add("Content-Type", "application/json");
	        headers.set("Accept", "text/vtt"); 

	        HttpEntity<String> entity = new HttpEntity<>(headers);

	        try {
	            ResponseEntity<String> response = restTemplate.exchange(
	                    transcriptContentUrl,
	                    HttpMethod.GET,
	                    entity,
	                    String.class
	            );

	            return response.getBody();

	        } catch (Exception ex) {
	            ex.printStackTrace();
	            throw new RuntimeException("Failed to get transcript content: " + ex.getMessage());
	        }
	    }
	    
	    public List<String> getMeetingActionItems(String meetingId) {
	        try {
	            String url = "https://graph.microsoft.com/beta/communications/callRecords/"
	                    + meetingId + "/aiInsights";
	            AccessToken token = graphClient.initializeMicrosoftGraph();
		        String accessToken = token.getToken();
	            HttpHeaders headers = new HttpHeaders();
	            headers.set("Authorization", "Bearer " + accessToken);
	            headers.set("Accept", "application/json");   // IMPORTANT
	            headers.set("Content-Type", "application/json");

	            HttpEntity<String> httpEntity = new HttpEntity<>(headers);

	            ResponseEntity<JsonNode> response = restTemplate.exchange(
	                    url,
	                    HttpMethod.GET,
	                    httpEntity,
	                    JsonNode.class
	            );

	            JsonNode root = response.getBody();
	            if (root == null || !root.has("insights")) {
	                return Collections.emptyList();
	            }

	            JsonNode insights = root.get("insights");

	            if (!insights.has("actionItems")) {
	                return Collections.emptyList();
	            }

	            JsonNode actionItemsNode = insights.get("actionItems");

	            List<String> actionItems = new ArrayList<>();

	            for (JsonNode item : actionItemsNode) {
	                if (item.has("content")) {
	                    actionItems.add(item.get("content").asText());
	                }
	            }

	            return actionItems;

	        } catch (Exception e) {
	            throw new RuntimeException("Failed to fetch action items: " + e.getMessage(), e);
	        }
	    }
	    
	    public JsonNode askCopilot(String transcript) {

	        String url = "https://directline.botframework.com/v3/directline/conversations";
            String directLineToken = "";
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + directLineToken);
	        headers.set("Content-Type", "application/json");

	        // Start conversation
	        ResponseEntity<JsonNode> startRes = restTemplate.exchange(
	                url,
	                HttpMethod.POST,
	                new HttpEntity<>(headers),
	                JsonNode.class
	        );

	        String conversationId = startRes.getBody().get("conversationId").asText();
	        String sendMessageUrl = url + "/" + conversationId + "/activities";
	        ObjectMapper objectMapper = new ObjectMapper();
	        // Create request body
	        ObjectNode msg = objectMapper.createObjectNode();
	        msg.put("type", "message");
	        msg.put("from", objectMapper.createObjectNode().put("id", "backend-service"));
	        msg.put("text", transcript);

	        // Send transcript
	        ResponseEntity<JsonNode> sendRes = restTemplate.exchange(
	                sendMessageUrl,
	                HttpMethod.POST,
	                new HttpEntity<>(msg.toString(), headers),
	                JsonNode.class
	        );

	        // Read reply from Copilot
	        String recvUrl = sendRes.getBody().get("id").asText();
	        ResponseEntity<JsonNode> reply = restTemplate.exchange(
	                url + "/" + conversationId + "/activities",
	                HttpMethod.GET,
	                new HttpEntity<>(headers),
	                JsonNode.class
	        );

	        return reply.getBody();
	    }
	
	    public String cleanTranscript(String raw) {
	        return raw.replaceAll("<v[^>]*>", "")                // remove <v Speaker> opening tags
	                  .replaceAll("</v>", "")                   // remove closing </v> tags
	                  .replaceAll("-->.*", "")                  // remove timestamps
	                  .replaceAll("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}", "") // remove hh:mm:ss.xxx
	                  .replaceAll("\\n{2,}", "\n")             // fix new lines
	                  .trim();
	    }
	    public String extractActionItems(String rawTranscript, String openAiKey) throws Exception {
	    	
	        String transcript = cleanTranscript(rawTranscript);

	        ObjectMapper mapper = new ObjectMapper();

	        ObjectNode root = mapper.createObjectNode();
	        root.put("model", "gpt-4o-mini");

	        ArrayNode messages = mapper.createArrayNode();

	        ObjectNode system = mapper.createObjectNode();
	        system.put("role", "system");
	        system.put("content",
	                "You are an AI assistant. Extract clear, concise ACTION ITEMS from this meeting transcript.");
	        messages.add(system);

	        ObjectNode user = mapper.createObjectNode();
	        user.put("role", "user");
	        user.put("content", transcript);
	        messages.add(user);

	        root.set("messages", messages);


	        OkHttpClient client = new OkHttpClient.Builder()
	                .callTimeout(Duration.ofSeconds(60))
	                .connectTimeout(Duration.ofSeconds(20))
	                .readTimeout(Duration.ofSeconds(40))
	                .build();
	        Request request = new Request.Builder()
	                .url("https://api.openai.com/v1/chat/completions")
	                .addHeader("Authorization", "Bearer " + openAiKey)
	                .post(RequestBody.create(mapper.writeValueAsString(root),
	                        MediaType.get("application/json; charset=utf-8")))
	                .build();

	        try (Response response = client.newCall(request).execute()) {

	            String responseBody = response.body().string();  // read once

	            log.info("the action items are:" + responseBody);

	            return responseBody;
	        }
	    }
	    
	    private static final String GRAPH_BASE = "https://graph.microsoft.com/beta";

		   
	    public JsonNode getMeetingInsights1(String meetingId) {
	        log.info("Insights method entered");

	        try {
	            // 1) Initialize token
	            AccessToken token = graphClient.initializeMicrosoftGraph();
	            this.accessToken = token.getToken();
                log.info("the access Token"+ this.accessToken);
	            // 2) Build URL
	         //   String url = GRAPH_BASE + "/users/{userId}/onlineMeetings/{meetingId}/recap";
//	            String url = GRAPH_BASE + "/communications/onlineMeetings/{meetingId}/recap";
//	           // url = url.replace("{userId}", "36f3cc24-2a46-4da3-a1f6-9f83e7bdd465");
//	            url = url.replace("{meetingId}", meetingId);
//	            
	            String url = "https://graph.microsoft.com/beta/copilot/users/" 
	                    + "36f3cc24-2a46-4da3-a1f6-9f83e7bdd465"
	                    + "/onlineMeetings/" + meetingId
	                    + "/aiInsights";

	            log.info("Calling URL: {}", url);

	            // 3) Prepare headers
	            HttpHeaders headers = new HttpHeaders();
	            headers.setBearerAuth(accessToken);
	            headers.set("Content-Type", "application/json");

	            HttpEntity<String> entity = new HttpEntity<>(headers);

	            // 4) Call API
	            ResponseEntity<JsonNode> res = restTemplate.exchange(
	                    url,
	                    HttpMethod.GET,
	                    entity,
	                    JsonNode.class
	            );

	            return res.getBody();
	        }

	        // -------------- SPECIFIC EXCEPTIONS --------------------

	        catch (HttpClientErrorException.BadRequest ex) {
	            log.error("400 Bad Request: {}", ex.getResponseBodyAsString());
	            throw new RuntimeException("Bad Request: " + ex.getResponseBodyAsString());
	        }

	        catch (HttpClientErrorException.NotFound ex) {
	            log.error("404 Not Found: {}", ex.getResponseBodyAsString());
	            throw new RuntimeException("Meeting or resource not found: " + ex.getResponseBodyAsString());
	        }

	        catch (HttpClientErrorException ex) {
	            log.error("Client Error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
	            throw new RuntimeException("Client error: " + ex.getResponseBodyAsString());
	        }

	        catch (HttpServerErrorException ex) {
	            log.error("Server Error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
	            throw new RuntimeException("Microsoft server error: " + ex.getResponseBodyAsString());
	        }

	        catch (RestClientException ex) {
	            log.error("RestTemplate Error: {}", ex.getMessage());
	            throw new RuntimeException("Error calling Microsoft Graph: " + ex.getMessage());
	        }

	        // -------------- GENERIC EXCEPTION --------------------

	        catch (Exception ex) {
	            log.error("General Exception: {}", ex.getMessage(), ex);
	            throw new RuntimeException("Unexpected error: " + ex.getMessage());
	        }
	    }

	
	    public JsonNode getMeetingByJoinUrl(String email, String joinUrl) {
            String organizedId ="36f3cc24-2a46-4da3-a1f6-9f83e7bdd465";
	        try {
	            // Build URL with filter (URL-encode the join URL)
	            String encodedJoinUrl = URLEncoder.encode(joinUrl, StandardCharsets.UTF_8);
	            String url = "https://graph.microsoft.com/v1.0/users/" 
	                         + organizedId 
	                         + "/onlineMeetings?$filter=joinWebUrl%20eq%20'" 
	                         + encodedJoinUrl + "'";

	            // Get access token
	            AccessToken token = graphClient.initializeMicrosoftGraph();
	            String accessToken = token.getToken();

	            HttpHeaders headers = new HttpHeaders();
	            headers.set("Authorization", "Bearer " + accessToken);
	            headers.set("Content-Type", "application/json");

	            HttpEntity<Void> entity = new HttpEntity<>(headers);

	            ResponseEntity<JsonNode> response =
	                    restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

	            return response.getBody();

	        } catch (Exception e) {
	            throw new RuntimeException("Graph API Error: " + e.getMessage());
	        }
	    }
	    
	    
	    public String sendToCopilot(String transcript, String accessToken) {
	        String agentUrl = "https://graph.microsoft.com/beta/copilot/agents/a6f59eff-34c5-f011-bbd3-000d3a1f9b48/actions";

	        RestTemplate restTemplate = new RestTemplate();
	        HttpHeaders headers = new HttpHeaders();
	        headers.setBearerAuth(accessToken);
	        headers.set("Content-Type", "application/json");

	        Map<String, Object> body = Map.of(
	                "inputs", Map.of("transcript", transcript)
	        );

	        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body, headers);
	        ResponseEntity<String> response = restTemplate.postForEntity(agentUrl, entity, String.class);
            log.info("the response is :"+ response.getBody());
	        return response.getBody();
	    }

	    
	    private void copilotService(String transcript) {
//	    	copilotService.sendFullTranscript(transcript).subscribe(jsonOutput ->
//	    	 {
//	    		 System.out.println("Structured JSON from Copilot:");
//		         System.out.println(jsonOutput);
//	    	 }); 
	    }
	    
	    public void copilotServiceTogetActionItems(String transcript) {
	    
	   
	    log.info("entered the method" + transcript);
	   // Mono<String> token = copilotService.startTokengeneration()
	   //         .doOnNext(t -> log.info("Token: {}", t));

	    copilotService.startTokengeneration()
	    .doOnNext(t -> log.info("Token: {}", t)).flatMap(token -> copilotService.sendFullTranscript(transcript,token)).subscribe(
	    		 response -> log.info("🎯 Controller received response:\n{}", response),
	             error -> log.error("❌ Error", error));
        //.doOnNext(t -> log.info("Token: {}", t)).flatMap(token -> copilotService.getValidToken(token))      // call getValidToken
        //.doOnNext(response -> {                                      // handle the DirectLineResponse
          //  log.info("DirectLineResponse received: {}", response.getToken());
            //log.info("DirectLineResponse received: {}", response.getConversationId());
            // You can extract data from response here, e.g.,
            // String conversationId = response.getConversationId();
            // String streamUrl = response.getStreamUrl();
      //  })
       // .doOnError(error -> log.error("Error while validating token: ", error))  // handle errors.flatMap(token -> copilotService.getValidToken(token))
        
	     
	    // log.info("entered the method" + token);
	    	//copilotService.getValidToken(accessToken);
	    	//copilotService.sendFullTranscript(transcript);
	    	
        }
	    
	    
	    public Mono<String> copilotServiceTogetActionItems1(String transcript) {

	        log.info("Entered copilotServiceTogetActionItems: {}", transcript);

	        return copilotService.startTokengeneration()
	                .doOnNext(token -> log.info("✅ Token generated"))
	                .flatMap(token ->
	                        copilotService.sendFullTranscript(transcript, token)
	                )
	                .doOnNext(response ->
	                        log.info("🎯 Controller received response:\n{}", response)
	                )
	                .doOnError(error ->
	                        log.error("❌ Error while calling Copilot", error)
	                );
	    }

}