package com.ikn.ums.serviceImpl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.google.common.net.MediaType;
import com.ikn.ums.dto.DirectLineResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CopilotService {
		
	private final String DIRECTLINE_URL = "https://directline.botframework.com/v3/directline/tokens/generate";
	
	private final String DIRECTLINE_URL_VALID= "https://directline.botframework.com/v3/directline/conversations";
	
	private final WebClient webClient;
	
	private CopilotService( @Value("${azure.bot.directline.secret}") String directLineSecret) {
		 this.webClient = WebClient.builder().
				          defaultHeader(HttpHeaders.AUTHORIZATION,"Bearer "+directLineSecret)
				          .defaultHeader(HttpHeaders.CONTENT_TYPE, "Application/json").build();
				 
		
		
	}
	// 1️⃣ Start getting token
    public Mono<String> startTokengeneration() {
        return webClient.post()
                .uri(DIRECTLINE_URL)
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> (String) resp.get("token"));
    }
    
    // 2 get Valid Token
    public Mono<DirectLineResponse> getValidToken(String token){
    	log.info("getValidToken() method is entered");
    	return webClient.post()
                .uri(DIRECTLINE_URL_VALID)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(DirectLineResponse.class);
                
    }
    String token_to_pass = "";
    //get Meeting transcript and pass it here
    public Mono<String> sendFullTranscript(String transcript, String token) {
       
        return getValidToken(token).flatMap(directLineResponse -> {
            String conversationId = directLineResponse.getConversationId();
            log.info("sendFullTranscript() the conversation Id:"+ conversationId);
            String directLineToken = directLineResponse.getToken();
          //  token_to_pass = token1;
            //log.info("sendFullTranscript() token :"+ token1);
            // Split transcript into chunks (10k chars)
            List<String> chunks = splitTranscript(transcript, 10000);
            String prompt = "Extract action items, meeting summary, discussion points, decisions made from transcript:";

            List<Mono<Void>> sendCalls = chunks.stream()
                    .map(chunk -> sendTranscriptChunk(conversationId,prompt ,chunk,directLineToken))
                    .collect(Collectors.toList());
            log.info("sendFullTranscript() method executed successfully");
            return Mono.when(sendCalls)
            		.then(Mono.delay(Duration.ofSeconds(2)))
                    .then(waitForFinalBotResponse(conversationId, directLineToken));
             
    }) .doOnNext(resp ->
    log.info("✅ FINAL BOT RESPONSE:\n{}", resp)
);
    }
    
    
    
 // 2️⃣ Send a transcript chunk asynchronously
//    public Mono<Void> sendTranscriptChunk(String conversationId, String transcriptChunk) {
//        Map<String, Object> body = Map.of(
//                "type", "message",
//                "from", Map.of("id", "user1"),
//                "text", transcriptChunk
//        );
//
//        return webClient.post()
//                .uri(DIRECTLINE_URL + "/" + conversationId + "/activities")
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(Void.class);
//    }
//    
    
    public Mono<Void> sendTranscriptChunk(
            String conversationId,
            String promptText, String transcript, String token
            
    ) {
    	
    	//List<Map<String, String>> transcript

       // Map<String, Object> attachmentContent = Map.of(
       //        "transcript", transcript
       // );

//        Map<String, Object> attachment = Map.of(
//                "contentType", "application/json",
//                "content", attachmentContent
//        );

        Map<String, Object> body = Map.of(
                "type", "message",
                "from", Map.of("id", "user1"),
                "text", promptText+transcript
        );

        return webClient.post()
                .uri(DIRECTLINE_URL_VALID + "/" + conversationId + "/activities")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }

    
 // 3️⃣ Get the latest bot response
    public Mono<String> getLatestBotResponse(String conversationId, String token) {
        return webClient.get()
                .uri(DIRECTLINE_URL_VALID + "/" + conversationId + "/activities")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(resp -> log.info("Full bot response: {}", resp))
                .map(resp -> {
                    List<Map<String, Object>> activities = (List<Map<String, Object>>) resp.get("activities");
                    return activities.stream()
                            .filter(a -> "bot".equals(((Map) a.get("from")).get("id")))
                            .map(a -> (String) a.get("text"))
                            .reduce("", (first, second) -> second); // take last bot response
                });
    }
    
    
    public Mono<Map<String, Object>> getLatestBotResponse4(String conversationId, String token) {
        return webClient.get()
                .uri(DIRECTLINE_URL_VALID + "/" + conversationId + "/activities")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(resp -> log.info("Full bot response: {}", resp))
                .map(resp -> {
                    List<Map<String, Object>> activities = (List<Map<String, Object>>) resp.get("activities");

                    // Get the last activity that has a 'value' field (structured JSON response)
                    return activities.stream()
                            .filter(a -> a.get("value") != null)
                            .map(a -> (Map<String, Object>) a.get("value"))
                            .reduce((first, second) -> second) // take last
                            .orElse(Map.of()); // empty map if none
                });
    }
    
    
    
    public Mono<String> getStructuredBotResponse(String conversationId, String token) {

        return webClient.get()
                .uri(DIRECTLINE_URL_VALID + "/" + conversationId + "/activities")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(resp -> {

                    List<Map<String, Object>> activities =
                            (List<Map<String, Object>>) resp.get("activities");

                    return activities.stream()
                            .filter(a -> a.containsKey("value"))
                            .map(a -> (Map<String, Object>) a.get("value"))
                            .map(v -> v.get("response"))
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .reduce((first, second) -> second)
                            .orElse("");
                });
    }
    
    public Mono<String> getStructuredBotResponse1(String conversationId, String token) {

        return webClient.get()
            .uri(DIRECTLINE_URL_VALID + "/" + conversationId + "/activities")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .map(resp -> {

                List<Map<String, Object>> activities =
                    (List<Map<String, Object>>) resp.get("activities");

                if (activities == null || activities.isEmpty()) {
                    return "{}"; // ✅ NEVER null
                }

                return activities.stream()
                    .filter(a -> a.get("value") != null)
                    .map(a -> (Map<String, Object>) a.get("value"))
                    .map(v -> v.get("response"))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .reduce((first, second) -> second)
                    .orElse("{}"); // ✅ NEVER null
            })
            .doOnNext(resp ->
                log.info("✅ Final structured bot response:\n{}", resp)
            );
    }


    
    
    
    
    // 4️⃣ Send full transcript efficiently
//    public Mono<String> sendFullTranscript(String transcript) {
//        return startConversation().flatMap(conversationId -> {
//            // Split transcript into chunks (10k chars per chunk)
//            List<String> chunks = splitTranscript(transcript, 10000);
//
//            // Send all chunks asynchronously
//            List<Mono<Void>> sendCalls = chunks.stream()
//                    .map(chunk -> sendTranscriptChunk(conversationId, chunk))
//                    .collect(Collectors.toList());
//
//            return Mono.when(sendCalls)
//                    .then(getLatestBotResponse(conversationId));
//        });
//    }
    
  
    
 // Helper: Split transcript into smaller chunks
    private List<String> splitTranscript(String transcript, int maxLength) {
        List<String> chunks = new ArrayList();
        int start = 0;
        while (start < transcript.length()) {
            int end = Math.min(start + maxLength, transcript.length());
            chunks.add(transcript.substring(start, end));
            start = end;
        }
        return chunks;
    }
    
    public Mono<String> waitForBotResponse(String conversationId, String token) {

        return Flux.interval(Duration.ofSeconds(2))
            .flatMap(i -> getStructuredBotResponse(conversationId, token))
            .filter(resp -> resp != null && !resp.isBlank() && !resp.equals("{}"))
            .next() // take first valid response
            .timeout(Duration.ofSeconds(20)) // fail after 20s
            .doOnNext(resp ->
                log.info("🤖 Bot response received:\n{}", resp)
            );
    }
    
    public Flux<Map<String, Object>> getLatestBotResponse1(String conversationId, String token) {

        return Flux.interval(Duration.ofSeconds(1))   // 🔁 poll every second
                .flatMap(tick ->
                        webClient.get()
                                .uri(DIRECTLINE_URL_VALID + "/" + conversationId + "/activities")
                                .header("Authorization", "Bearer " + token)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                )
                .flatMap(resp -> {

                    List<Map<String, Object>> activities =
                            (List<Map<String, Object>>) resp.get("activities");

                    return Flux.fromIterable(activities)
                            .filter(a -> a.containsKey("value"))   // ✅ bot response lives here
                            .map(a -> (Map<String, Object>) a.get("value"))
                            .next();   // ✅ Mono<Map<String,Object>>
                })
                .timeout(Duration.ofSeconds(20))   // ⏱ wait max 20s
                .doOnNext(map ->
                        log.info("✅ FINAL BOT RESPONSE MAP:\n{}", map)
                );
    }
    
    
    public Mono<String> getFinalStructuredResponse(String conversationId, String token) {
      
    	log.info("getFinalStructuredResponse() is entered");
        return webClient.get()
                .uri(DIRECTLINE_URL_VALID + "/" + conversationId + "/activities")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(resp-> log.info("🤖 Bot response received:\n{}", resp))
                
                .flatMap(resp -> {

                    List<Map<String, Object>> activities =
                            (List<Map<String, Object>>) resp.get("activities");

                    if (activities == null || activities.isEmpty()) {
                        return Mono.empty(); // ✅ Mono
                    }

                    return Flux.fromIterable(activities)
                            .filter(a -> "event".equals(a.get("type")))
                            .map(a -> (Map<String, Object>) a.get("value"))
                            .filter(Objects::nonNull)
                            .map(v -> v.get("response"))
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .next(); // 🔥 THIS IS MANDATORY
                }).doOnNext(resp -> log.info("✅ FINAL STRUCTURED RESPONSE:\n{}", resp));
               // .doOnEmpty(() -> log.warn("⚠️ No structured response from bot"));
    }

    
    private String cleanMarkdownJson(String raw) {
        return raw
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
    
    public Mono<String> waitForFinalBotResponse1(String conversationId, String token) {

        return Mono.defer(() -> getFinalStructuredResponse(conversationId, token))
                .repeatWhenEmpty(repeat ->
                        repeat.delayElements(Duration.ofSeconds(2))
                              .take(10)   // max 20 seconds wait
                )
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Bot did not respond in time")
                ));
    }
    
    public Mono<String> waitForFinalBotResponse(String conversationId, String token) {

        return Flux.interval(Duration.ZERO, Duration.ofSeconds(3))
                .flatMap(tick -> getFinalStructuredResponse(conversationId, token))
                .filter(response -> response != null && !response.isBlank())
                .next() // take first FINAL response
                .timeout(Duration.ofSeconds(90))
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Bot did not respond in time")
                ));
    }



    

}
