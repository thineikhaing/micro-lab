package org.tek.microlab.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tek.microlab.S3FileEvent;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ProcessService {

    private final S3Client s3Client;

    @Autowired
    private S3FileEventRepository eventRepo;

    @Value("${aws.s3.upload-bucket-name}")
    private String uploadBucketName;

    @Value("${aws.s3.promote-bucket-name}")
    private String promoteBucketName;

    public void uploadFile(MultipartFile file, String fileKey) throws IOException {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(uploadBucketName)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

    }

    public byte[] downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(promoteBucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(getObjectRequest)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }
}