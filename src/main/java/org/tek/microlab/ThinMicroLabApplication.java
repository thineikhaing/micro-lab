package org.tek.microlab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.tek.microlab.common.SignalFileBatchJobAdapter;
import org.tek.microlab.dms.DmsPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
//@SpringBootApplication
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
public class ThinMicroLabApplication extends SpringBootServletInitializer implements CommandLineRunner {

    @Autowired
    private DmsPort dmsPort;
    @Autowired
    private SignalFileBatchJobAdapter signalFileBatchJobAdapter;

    public static void main(String[] args) {
        boolean isBatch = Arrays.stream(args).anyMatch(arg -> arg.startsWith("BATCH-JOB"));
        new SpringApplicationBuilder(ThinMicroLabApplication.class)
                .web(isBatch ? WebApplicationType.NONE : WebApplicationType.SERVLET)
                .run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ThinMicroLabApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
//        log.info("Starting batch job execution with arguments: {}", Arrays.toString(args));
        Optional<String> batchJobId = parseBatchJobId(args);
        if (batchJobId.isEmpty()) {
            log.info("No batch job ID specified");
            return;
        }
        executeBatchJob(batchJobId.get());
    }

    private Optional<String> parseBatchJobId(String[] args) {
        return Arrays.stream(args)
                .filter(arg -> arg.startsWith("BATCH-JOB="))
                .map(arg -> arg.split("="))
                .filter(parts -> parts.length == 2)
                .map(parts -> parts[1])
                .findFirst(); // Takes the first valid BATCH-JOB argument
    }

    private void executeBatchJob(String batchJobId) {
        switch (batchJobId.toUpperCase()) { // Use toUpperCase for case-insensitive comparison
            case Constants.BATCH_JOB_ID.DMS:
                log.info("----- Executing DMS Connectivity Batch Job -----");
                runDmsConnectivityJob();
                break;
            case Constants.BATCH_JOB_ID.S3PROCESS:
                log.info("----- Executing S3 File Process Batch Job -----");
                runS3FileProcessJob();
                break;
            case Constants.BATCH_JOB_ID.SIGNALFILE:
                log.info("----- Executing Signal File Process Batch Job -----");
                runSignalFileJob();
                break;
            default:
                log.warn("Unrecognized batch job ID: {}. No action performed.", batchJobId);
                break;
        }
    }

    private void runDmsConnectivityJob() {
        try {
            byte[] dummy = "Hello World - Sharepoint Uploader [NTLM]".getBytes();
            dmsPort.sendFile("test.txt", dummy, "BATCH");
            log.info("DMS sendFile() completed");
        } catch (Exception e) {
            log.error("Error executing DMS Connectivity Batch Job: {}", e.getMessage(), e);
            // Handle specific exceptions or rethrow as needed
        }
    }

    private void runS3FileProcessJob() {
        // Implement your S3 file processing logic here
        // For example:
        // s3ProcessorService.processFiles();
        log.info("S3 File Process Batch Job logic executed (placeholder).");
    }

    private void runSignalFileJob(){
        log.info("Signal File Process Batch Job logic executed.");
        signalFileBatchJobAdapter.GenerateSignalFile();
        log.info("Generate Signal File Batch Job completed");
    }

}