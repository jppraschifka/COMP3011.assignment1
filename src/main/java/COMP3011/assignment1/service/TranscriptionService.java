package COMP3011.assignment1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import COMP3011.assignment1.dto.OpenAiTranscriptionResponse;

@Service
public class TranscriptionService {

    private final RestClient restClient;
    private final StatisticsService statisticsService;
    private final String apiKey;

    public TranscriptionService(
            RestClient.Builder restClientBuilder,
            StatisticsService statisticsService,
            @Value("${OPENAI_API_KEY:}") String apiKey) {

        this.restClient = restClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .build();

        this.statisticsService = statisticsService;
        this.apiKey = apiKey;
    }

    public String transcribe(MultipartFile file) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is not configured.");
        }

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        bodyBuilder
                .part("file", file.getResource())
                .filename(
                        file.getOriginalFilename() != null
                                ? file.getOriginalFilename()
                                : "recording.webm");

        bodyBuilder.part(
                "model",
                "gpt-4o-mini-transcribe");

        OpenAiTranscriptionResponse response = restClient
                .post()
                .uri("/audio/transcriptions")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(OpenAiTranscriptionResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "No response received from transcription service.");
        }

        if (response.usage() != null) {
            statisticsService.addTokenUsage(
                    response.usage().inputTokens(),
                    response.usage().outputTokens());
        }

        return response.text();
    }
}