//package org.tek.microlab.aws;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.awspring.cloud.sqs.annotation.SqsListener;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.time.Instant;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class SqsMessageConsumer {
//    @Autowired
//    S3ProcessService s3ProcessService;
//
////    @Autowired
////    private S3FileEventRepository eventRepo;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @SqsListener("thin-s3-notification")
//    public void listen(String message) throws JsonProcessingException {
//        JsonNode root = objectMapper.readTree(message);
//        JsonNode records = root.path("Records");
//
//        for (JsonNode record : records) {
//            String bucketName = record.path("s3").path("bucket").path("name").asText();
//            String objectKey = record.path("s3").path("object").path("key").asText();
//
//            // Process event
//            System.out.printf("New object created: s3://%s/%s%n", bucketName, objectKey);
//
//             byte[] file = s3ProcessService.downloadFile(objectKey);
//            InputStream excelInputStream = new ByteArrayInputStream(file);
//
//            // Update DB record
////            eventRepo.findByObjectKey(objectKey).ifPresent(event -> {
////                event.setStatus("PROCESSED");
////                event.setUpdatedAt(Instant.now());
////                eventRepo.save(event);
////            });
//
//            log.info("Download file: {}",excelInputStream);
//        }
//    }
//}