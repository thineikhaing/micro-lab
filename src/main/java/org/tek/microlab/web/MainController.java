package org.tek.microlab.web;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tek.microlab.S3FileEvent;
import org.tek.microlab.aws.S3FileEventRepository;
import org.tek.microlab.aws.S3PollingService;
import org.tek.microlab.aws.S3ProcessService;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class MainController {
    private final S3ProcessService s3ProcessService;
    @Autowired
    private S3FileEventRepository eventRepo;
    private final S3PollingService s3PollingService;
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {

        String originalFilename = file.getOriginalFilename();
        String fileKey = UUID.randomUUID().toString() + "-" + originalFilename;

        // Step 1: Create DB record
        UUID fileId = UUID.randomUUID();
        S3FileEvent event = S3FileEvent.builder()
                .id(fileId)
                .objectKey(fileKey)
                .status("UPLOADED")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        eventRepo.save(event);

        // Step 2: Upload to S3
        s3ProcessService.uploadFile(file, fileKey);

        // Step 3: Poll in a new transaction for latest status
        Optional<S3FileEvent> finalEvent = s3PollingService.pollUntilProcessed(fileKey, 6, Duration.ofSeconds(5));

        if (finalEvent.isPresent()) {
            String status = finalEvent.get().getStatus();
            if ("PROCESSED".equals(status)) {
                return ResponseEntity.ok("File processed: " + fileKey);
            } else if ("FAILED".equals(status)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Processing failed: " + fileKey);
            }
        }
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Timeout waiting for S3 event.");}

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("bucket") String bucket,
                                           @RequestParam("key") String key) {
        byte[] file = s3ProcessService.downloadFile(key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .body(file);
    }

    @PostMapping("/uploadAsync")
    public ResponseEntity<Map<String, String>> uploadAsync(@RequestParam("file") MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String fileKey = UUID.randomUUID().toString() + "-" + originalFilename;

        // Step 1: Create DB record
        UUID fileId = UUID.randomUUID();
        S3FileEvent event = S3FileEvent.builder()
                .id(fileId)
                .objectKey(fileKey)
                .status("UPLOADED") // Initial status, will be updated by the background processor
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        eventRepo.save(event);

        // Step 2: Upload to S3 (this is fast)
        // This should ideally trigger an asynchronous process (e.g., S3 event -> Lambda/SQS/another service)
        s3ProcessService.uploadFile(file, fileKey);

        // Immediately return a 202 Accepted with the file ID
        // The client should then use this fileId to poll a separate /status endpoint
        Map<String, String> response = new HashMap<>();
        response.put("fileId", fileKey);
        response.put("status", event.getStatus());
        response.put("message", "File upload initiated. Processing will continue in the background. Please check status endpoint for updates.");
        return ResponseEntity.accepted().body(response); // 202 Accepted
    }

    // New endpoint for checking status
    @GetMapping("/status/{objectKey}")
    public ResponseEntity<S3FileEvent> getFileStatus(@PathVariable String objectKey) {
        return eventRepo.findByObjectKey(objectKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
