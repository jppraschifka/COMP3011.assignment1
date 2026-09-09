package COMP3011.assignment1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import COMP3011.assignment1.service.TranscriptionService;

@RestController
@RequestMapping("/api/v1")
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    public TranscriptionController(
            TranscriptionService transcriptionService) {

        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("No audio file was received.");
        }

        try {

            String transcription =
                    transcriptionService.transcribe(file);

            return ResponseEntity.ok(transcription);

        } catch (RestClientResponseException exception) {

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("The transcription service request failed.");

        } catch (Exception exception) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to transcribe the recording.");
        }
    }
}