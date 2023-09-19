package com.example.chatgptsummarizer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class ChatGPTSummarizerApplication {

    @Value("${app.openai-api-key}")
    private String openaiApiKey;

    public static void main(String[] args) {
        SpringApplication.run(ChatGPTSummarizerApplication.class, args);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .build();
    }
}

@RestController
class SummarizeController {

    private final WebClient webClient;

    SummarizeController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping(value = "/summarize", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> summarize(@RequestBody String content) {
        return webClient.post()
                .bodyValue(new ChatGPTRequest("Summarize the following (keep in same language, shorten): " + content))
                .retrieve()
                .bodyToMono(ChatGPTResponse.class)
                .map(ChatGPTResponse::text);
    }

}

record ChatGPTMessage(
        String role,
        String content
) {
    ChatGPTMessage(String content) {
        this("user", content);
    }
}

record ChatGPTRequest(
        String model,
        List<ChatGPTMessage> messages
) {
    ChatGPTRequest(String message) {
        this("gpt-3.5-turbo", Collections.singletonList(new ChatGPTMessage(message)));
    }
}

record ChatGPTChoice(
        ChatGPTMessage message
) {
}

record ChatGPTResponse(
        List<ChatGPTChoice> choices
) {
    public String text() {
        return choices.get(0).message().content();
    }
}
