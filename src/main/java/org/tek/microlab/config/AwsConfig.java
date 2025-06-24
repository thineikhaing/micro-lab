//package org.tek.microlab.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.sqs.SqsClient;
//
//@Configuration
//public class AwsConfig {
//
//    @Value("${aws.region:ap-southeast-1}") // Default to Singapore region
//    private String awsRegion;
//
//    @Value("${aws.credentials.access-key}")
//    private String accessKey;
//
//    @Value("${aws.credentials.secret-key}")
//    private String secretKey;
//
//    @Bean
//    public S3Client s3Client() {
//        return S3Client.builder()
//                .region(Region.of(awsRegion))
//                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
//                .build();
//    }
//
//    @Bean
//    public SqsClient sqsClient() {
//        return SqsClient.builder()
//                .region(Region.of(awsRegion))
//                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
//                .build();
//    }
//}