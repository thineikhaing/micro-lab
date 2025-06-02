package org.tek.microlab.aws;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tek.microlab.S3FileEvent;

import java.util.Optional;
import java.util.UUID;

public interface S3FileEventRepository extends JpaRepository<S3FileEvent, UUID> {
    Optional<S3FileEvent> findByObjectKey(String objectKey);

    @Query(value = "SELECT * FROM s3_file_events WHERE object_key = :objectKey", nativeQuery = true)
    Optional<S3FileEvent> findFreshByObjectKey(@Param("objectKey") String objectKey);

}