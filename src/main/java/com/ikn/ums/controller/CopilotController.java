package com.ikn.ums.controller;

import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.ikn.ums.serviceImpl.TeamsMeetingService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
public class CopilotController {
	
	private final String DIRECT_LINE_SECRET = "";
	
	 @Autowired
	 private TeamsMeetingService service;
	
	  @GetMapping("/token")
	    public ResponseEntity<String> getDirectLineToken() {
	        RestTemplate rest = new RestTemplate();
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + DIRECT_LINE_SECRET);

	        HttpEntity<String> entity = new HttpEntity<>("", headers);

	        String url = "https://directline.botframework.com/v3/directline/tokens/generate";

	        ResponseEntity<String> response = rest.postForEntity(url, entity, String.class);
	        return ResponseEntity.ok(response.getBody());
	    }
	
	  @GetMapping("/{meetingId}/summary")
	    public JsonNode getSummary(@PathVariable String meetingId) throws Exception {
	        return service.getCopilotSummary(meetingId);
	    }

	    @GetMapping("/meetingTranscript/{meetingId}/transcripts")
	    public Object getTranscriptMetadata(@PathVariable String meetingId) throws Exception {
	        return service.getTranscriptsMetadata(meetingId);
	    }
	    
	    @GetMapping("/meetingData/{meetingId}/{email}")
	    public JsonNode getMetadata(@PathVariable String meetingId, @PathVariable String email) throws Exception {
	    	//String email = "bharat@ikcontech.com";
	    //	String email = "anil.pamarthi@ikcontech.com";
	        return service.getMeetingData(email,meetingId);
	    }

	    @GetMapping("/users/{email}")
	    public String getMetaUsers(@PathVariable String email) throws Exception {
	    	log.info("entered the getMetausers Data():");
	        return service.getUserIdFromEmail(email);
	    }
	    
	    @GetMapping("/ai/{meetingId}")
	    public List<String> getActionItems(@PathVariable String meetingId) throws Exception {
	    	
	        return service.getMeetingActionItems(meetingId);
	    }



//	    @GetMapping("/download")
//	    public void downloadTranscript(@RequestParam String url, HttpServletResponse response) throws Exception {
//	        InputStream inputStream = service.downloadTranscriptFile(url);
//
//	        response.setContentType("application/octet-stream");
//
//	        OutputStream out = response.getOutputStream();
//	        inputStream.transferTo(out);
//	        out.flush();
//	    }
//	
	    @GetMapping("/insights/{meetingId}")
	    public ResponseEntity<?> getInsights(@PathVariable String meetingId) {
	        try {
	            JsonNode insights = service.getMeetingInsights1(meetingId);
	            return ResponseEntity.ok(insights);

	        } catch (Exception e) {
	            return ResponseEntity.status(500).body("Error: " + e.getMessage());
	        }
	    }
	    
	    @PostMapping("/genActionItems")
	    public ResponseEntity<Mono<String>> getCopilotData(@RequestBody String meetingTranscriptData) {
    
	        Mono<String> responseMono = service.copilotServiceTogetActionItems1(meetingTranscriptData)
	                .doOnSubscribe(sub -> log.info("🚀 Copilot request started"))
	                .doOnNext(resp -> log.info("✅ Copilot response received"))
	                .onErrorResume(ex -> {
	                    log.error("❌ Error while generating action items", ex);
	                    return Mono.just(
	                            "{ \"error\": \"Failed to generate action items. Please try again later.\" }"
	                    );
	                });

	        return ResponseEntity.ok(responseMono);
	    }



}
