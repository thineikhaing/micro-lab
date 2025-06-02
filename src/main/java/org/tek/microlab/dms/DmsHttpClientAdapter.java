package org.tek.microlab.dms;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.tek.microlab.Constants;
import org.tek.microlab.exception.DMSServiceException;
import org.tek.microlab.dms.dto.DmsFilePropertiesDto;
import org.tek.microlab.dms.dto.DmsFilePropertiesMetadataDto;
import org.tek.microlab.dms.dto.DmsFilePropertiesMetadataTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class DmsHttpClientAdapter implements DmsPort{
	private static final Logger logger = LoggerFactory.getLogger(DmsHttpClientAdapter.class);

	@Value("${dms.marketdata.upload-file.url}")
	private String dmsMarketdataUploadFileUrl;

	@Value("${dms.marketdata.url}")
	private String dmsMarketdataPropertiesUrl;
	
	@Value("${dms.marketdata.data}")
	private String dmsMarketdataPropertiesData;

	private final CloseableHttpClient ntlmHttpClient;

	private final SharepointContextAdapter sharepointContextAdapter;

	private final ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void sendFile(String filename, byte[] file, String type) {
		logger.info("Calling DMS Server for send file");

		// HttpClientContext is crucial for persistent NTLM authentication state
		HttpClientContext context = HttpClientContext.create();

		try {
			logger.info("Authenticating with DMS Provider to get FormDigestValue...");
			// Use the SharepointContextAdapter to get the digest value.
			// Note: sharepointContextAdapter internally creates its own HttpClient.
			// If you want to reuse the same HttpClient for digest too,
			// SharepointContextAdapter would need a constructor that takes CloseableHttpClient and HttpClientContext.
			// For now, let's keep it separate if it's already working.
			String authenticationToken = sharepointContextAdapter.getDigestValue();
			logger.info("Received digest token: {}", authenticationToken);

			// 1. Upload file
			logger.info("Connecting DMS for file upload: {}", dmsMarketdataUploadFileUrl.replace("replacefilename", filename));
			HttpPost uploadPost = new HttpPost(dmsMarketdataUploadFileUrl.replace("replacefilename", filename));
			uploadPost.setHeader("x-requestdigest", authenticationToken);
			uploadPost.setEntity(new ByteArrayEntity(file, ContentType.APPLICATION_OCTET_STREAM));

			try (CloseableHttpResponse uploadResponse = ntlmHttpClient.execute(uploadPost, context)) {
				int statusCode = uploadResponse.getCode();
				String responseBody = EntityUtils.toString(uploadResponse.getEntity(), StandardCharsets.UTF_8);
				if (statusCode >= 200 && statusCode < 300) {
					logger.info("File upload successful. Status: {}", statusCode);
				} else {
					logger.error("File upload failed. Status: {}, Response: {}", statusCode, responseBody);
					throw new IOException("File upload failed with status code " + statusCode + ": " + responseBody);
				}
			}

			// 2. Add compulsory details for check-in (metadata)
			logger.info("Connecting DMS for file details update");
			String dmsPropertiesUrl = dmsMarketdataPropertiesUrl.replace("replacefilename", filename);

			DmsFilePropertiesDto fileCheckInDto = new DmsFilePropertiesDto();
			List<DmsFilePropertiesMetadataDto> metaList = new ArrayList<>();
			DmsFilePropertiesMetadataTypeDto metadataType = new DmsFilePropertiesMetadataTypeDto();
			metadataType.setType("SP.ListItemFormUpdateValue");

			for (String fieldTemp : dmsMarketdataPropertiesData.split(";")) {
				String[] fieldDetail = fieldTemp.split("=");
				DmsFilePropertiesMetadataDto metadata = new DmsFilePropertiesMetadataDto();
				metadata.setFieldName(fieldDetail[0]);
				metadata.setFieldValue(fieldDetail[1]);
				metadata.setMetadataType(metadataType);
				metaList.add(metadata);

				logger.info("DMS File Properties Metadata Field Name: {}", metadata.getFieldName());
				logger.info("DMS File Properties Metadata Field Value: {}", metadata.getFieldValue());
			}
			fileCheckInDto.setMetadata(metaList);
			fileCheckInDto.setBNewDocumentUpdate(true);

			HttpPost metadataPost = new HttpPost(dmsPropertiesUrl);
			metadataPost.setHeader("x-requestdigest", authenticationToken);
			metadataPost.setHeader("content-type", "application/json;odata=verbose");
			metadataPost.setHeader("accept", "application/json;odata=verbose");

			String payload = objectMapper.writeValueAsString(fileCheckInDto);
			logger.debug("Metadata Update Request Payload: {}", payload);
			metadataPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));


			try (CloseableHttpResponse metadataResponse = ntlmHttpClient.execute(metadataPost, context)) {
				int statusCode = metadataResponse.getCode();
				String responseBody = EntityUtils.toString(metadataResponse.getEntity(), StandardCharsets.UTF_8);
				if (statusCode >= 200 && statusCode < 300) {
					logger.info("File metadata update successful. Status: {}", statusCode);
				} else {
					logger.error("File metadata update failed. Status: {}, Response: {}", statusCode, responseBody);
					throw new IOException("File metadata update failed with status code " + statusCode + ": " + responseBody);
				}
			}

			logger.info("File sent successfully to DMS");

		} catch (ServiceUnavailable | org.springframework.web.client.ResourceAccessException e) { // Keep these specific exceptions if they are thrown by your current setup
			if (Constants.MARKET_DATA_REPORT_TYPE.BATCH.equalsIgnoreCase(type)) {
				logger.error("Batch processing error (AMO02031, AMO32013) due to DMS service unavailability or resource access issue.");
				throw e;
			} else {
				logger.error("Unable to connect to DMS.");
				logger.error(e.getMessage(), e);
				throw new DMSServiceException();
			}
		} catch (IOException | ParseException e) { // Catch ParseException from EntityUtils.toString
			if (Constants.MARKET_DATA_REPORT_TYPE.BATCH.equalsIgnoreCase(type)) {
				logger.error("Batch processing error (AMO02032, AMO32014) during DMS file operations.");
				throw new RuntimeException(e); // Wrap in RuntimeException for broader compatibility, or define a specific DMSServiceException sub-type
			} else {
				logger.error("Sending market data report to DMS failed with exception.");
				logger.error(e.getMessage(), e);
				throw new DMSServiceException();
			}
		}
	}

}
