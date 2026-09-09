const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("status");

let mediaRecorder;
let audioChunks = [];

startButton.addEventListener("click", async () => {

    try {

        const stream = await navigator.mediaDevices.getUserMedia({
            audio: true
        });

        audioChunks = [];

        mediaRecorder = new MediaRecorder(stream);

        mediaRecorder.addEventListener("dataavailable", event => {
            audioChunks.push(event.data);
        });

        mediaRecorder.addEventListener("stop", () => {

            const audioBlob = new Blob(audioChunks, {
                type: mediaRecorder.mimeType
            });

            console.log("Recording complete");
            console.log("Audio type:", audioBlob.type);
            console.log("Audio size:", audioBlob.size);

            stream.getTracks().forEach(track => track.stop());

            statusText.textContent = "Recording complete";
        });

        mediaRecorder.start();

        statusText.textContent = "Recording...";
        startButton.disabled = true;
        stopButton.disabled = false;

    } catch (error) {

        console.error("Microphone error:", error);

        statusText.textContent = "Unable to access microphone";
    }
});


stopButton.addEventListener("click", () => {

    if (mediaRecorder && mediaRecorder.state === "recording") {

        mediaRecorder.stop();

        startButton.disabled = false;
        stopButton.disabled = true;
    }
});