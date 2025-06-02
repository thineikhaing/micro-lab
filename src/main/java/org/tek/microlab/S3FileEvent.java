package org.tek.microlab;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "s3_file_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class S3FileEvent {
    @Id
    private UUID id;
    private String objectKey;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}