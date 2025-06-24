### S3 workflow
- Upload file → to Bucket 1 [upload-bucket]
- Lambda scans for virus:
  - If clean, put in Bucket 3  [promote-bucket]
  - If infected, put in Bucket 2  [quarantine-bucket]
- S3 sends event notification (via SQS) for Bucket 2 (infected) and Bucket 3 (clean)

### Spring Boot app:

- Uploads file to Bucket 1 
- Update DB with **UPLOADED** status
- Listens to SQS queues from Bucket 2 and Bucket 3
- Parses message
- If clean → fetches file from Bucket 3
  - Update DB with **PROCESSED** status  
- If infected → handle accordingly
  - Update DB with **INFECTED** status
- Synchronous Polling for db-status check


### NTLM Sharepoint Uploader
```
mvn clean package
```
```
java -jar target/ammo-dms-batch.jar
java -jar target/ammo-dms-batch.jar BATCH-JOB=SIGNALFILE

```

