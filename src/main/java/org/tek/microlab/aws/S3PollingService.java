//package org.tek.microlab.aws;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.tek.microlab.S3FileEvent;
//
//import java.time.Duration;
//import java.util.Optional;
//import jakarta.persistence.EntityManager;
//
//@Service
//@RequiredArgsConstructor
//public class S3PollingService {
//
////    private final S3FileEventRepository eventRepo;
////    private final EntityManager entityManager;
//
//    public Optional<S3FileEvent> pollUntilProcessed(String fileKey, int maxRetries, Duration waitBetweenRetries) throws InterruptedException {
//        for (int i = 0; i < maxRetries; i++) {
//            Thread.sleep(waitBetweenRetries.toMillis());
//
//            // Clear Hibernate first-level cache
////            entityManager.clear();
////
////            S3FileEvent updated = eventRepo.findFreshByObjectKey(fileKey).orElse(null);
////            System.out.println("Polling for file ID: " + fileKey + ", attempt: " + (i + 1)  + ", current status: "  + (updated != null ? updated.getStatus() : "not found"));
////
////            if (updated != null) {
////                String status = updated.getStatus();
////                if ("PROCESSED".equals(status)) return Optional.of(updated);
////                if ("FAILED".equals(status)) return Optional.of(updated); // optionally handle failed
////            }
//        }
//        return Optional.empty();
//    }
//}