package COMP3011.assignment1.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import COMP3011.assignment1.service.TranscriptionService;

class TranscriptionControllerTest {

    private MockMvc mockMvc;
    private TranscriptionService transcriptionService;

    @BeforeEach
    void setUp() {

        transcriptionService = mock(TranscriptionService.class);

        TranscriptionController controller =
                new TranscriptionController(transcriptionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void validAudioReturnsTranscription() throws Exception {

        MockMultipartFile audio = new MockMultipartFile(
                "file",
                "recording.webm",
                "audio/webm",
                "test audio".getBytes());

        when(transcriptionService.transcribe(any(MultipartFile.class)))
                .thenReturn("Hello from the test.");

        mockMvc.perform(multipart("/api/v1/transcribe")
                        .file(audio))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Hello from the test."));
    }

    @Test
    void emptyAudioReturnsBadRequest() throws Exception {

        MockMultipartFile audio = new MockMultipartFile(
                "file",
                "recording.webm",
                "audio/webm",
                new byte[0]);

        mockMvc.perform(multipart("/api/v1/transcribe")
                        .file(audio))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "No audio file was received."));
    }

    @Test
    void sttFailureReturnsBadGateway() throws Exception {

        MockMultipartFile audio = new MockMultipartFile(
                "file",
                "recording.webm",
                "audio/webm",
                "test audio".getBytes());

        when(transcriptionService.transcribe(any(MultipartFile.class)))
                .thenThrow(
                        new HttpServerErrorException(
                                HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(multipart("/api/v1/transcribe")
                        .file(audio))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(
                        "The transcription service request failed."));
    }
}