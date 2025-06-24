package org.tek.microlab.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SignalFileBatchJobAdapter {


    @Value("${fmbs.upload.traderesults.file}")
    public String importFileName;

    @Value("${fmbs.upload.signal.file}")
    public String fmbsSignalFile;

    @Value("${fmbs.upload.traderesults.directory}")
    public String fmbsDirectory;

    public void GenerateSignalFile() {
        List<String> fileNameList = new ArrayList<>();
        String fmbsFileName = "";
        boolean auctionEnd = true;
        fmbsFileName = String.join(File.separator, fmbsDirectory, importFileName);

        // 1.1 Generation of signal file for FMBS - To activate the SFTP process via
        // Autosys.
        fileNameList.add(fmbsFileName);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startDateTime = currentTime.minusMinutes(20);
        checkandGenerateSignalFile(fileNameList, fmbsSignalFile, startDateTime, auctionEnd);
        fileNameList.clear();

    }

    private boolean checkandGenerateSignalFile(List<String> fileList, String signalFile, LocalDateTime startTime, boolean auctionEnd) {
        boolean allMatch = false;
        boolean created = false;
        allMatch = fileList.stream().filter(Objects::nonNull).allMatch(file -> {
            boolean allExists = false;
            allExists = checkFile(file, startTime);
            return allExists;
        });
        log.info("fileList: {}  ", fileList);
        log.info("allMatch: {}  ", allMatch);
        log.info("auctionEnd: {}  ", auctionEnd);
        log.info("signalFile: {}", signalFile);
        if (allMatch && auctionEnd) {
            File file = new File(signalFile);
            if (!file.exists()) {
                try {
                    created = file.createNewFile();
                    log.info("Signal file not found. Creating new signal file: {}", signalFile);
                } catch (IOException e) {
                    log.error("Error generating signal File {} ", e.getMessage());
                }
            }else{
                log.info("Signal file already exists: {}", signalFile);
            }
        }

        if(auctionEnd){
            File file = new File(signalFile);
            try {
                created = file.createNewFile();
                log.info("Signal file not found. Creating new signal file: {}", signalFile);
            } catch (IOException e) {
                log.error("Error generating signal File {} ", e.getMessage());
            }
        }
        return created;
    }

    private boolean checkFile(String fileName, LocalDateTime startTime) {
        boolean isFileExists = false;
        Path file = Paths.get(fileName);
        if (file.toFile().exists()) {
            BasicFileAttributes attr;
            try {
                attr = Files.readAttributes(file, BasicFileAttributes.class);
                isFileExists = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneOffset.systemDefault())
                        .isAfter(startTime);
                log.info("file creation time: {}",LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneOffset.systemDefault()));
                log.info("startTime {}",startTime);
                log.info("isFileExists {}",isFileExists);
            } catch (IOException e) {
                log.error("Error in checking the file generated is after auction started");
            }

        }
        return isFileExists;
    }

}
