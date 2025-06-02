package org.tek.microlab.dms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.auth.*;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Component
@RequiredArgsConstructor
public class SharepointContextAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SharepointContextAdapter.class);

    @Value("${dms.userid}")
    private String dmsUserId;

    @Value("${dms.password}")
    private String dmsPassword;

    @Value("${dms.authentication.url}")
    private String dmsAuthenticationUrl;

    public String getDigestValue() throws IOException {
        logger.info("Calling DMS Server to retrieve FormDigestValue");
        String formDigestValue = null;

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new NTCredentials(dmsUserId, dmsPassword.toCharArray(), null, null);
        ((BasicCredentialsProvider) credsProvider).setCredentials(new AuthScope(null, -1), credentials);

        Lookup<AuthSchemeFactory> authSchemeRegistry = RegistryBuilder.<AuthSchemeFactory>create()
                .register("NTLM", new NTLMSchemeFactory())
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setTargetPreferredAuthSchemes(List.of("NTLM"))
                .build();

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultAuthSchemeRegistry(authSchemeRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            HttpClientContext context = HttpClientContext.create();
            HttpPost authPost = new HttpPost(dmsAuthenticationUrl);
            authPost.setEntity(new ByteArrayEntity(new byte[0], ContentType.APPLICATION_OCTET_STREAM));

            try (CloseableHttpResponse authResponse = httpclient.execute(authPost, context)) {
                int statusCode = authResponse.getCode();
                String body = EntityUtils.toString(authResponse.getEntity(), StandardCharsets.UTF_8);
                logger.debug("Response Body from authentication endpoint: {}", body); // Log the entire body

                if (statusCode == 200) {
                    try {
                        // Parse XML instead of JSON
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
                        doc.getDocumentElement().normalize();

                        NodeList nodeList = doc.getElementsByTagName("d:FormDigestValue");
                        if (nodeList.getLength() > 0) {
                            Element formDigestElement = (Element) nodeList.item(0);
                            formDigestValue = formDigestElement.getTextContent();
                            logger.info("Successfully retrieved FormDigestValue: {}", formDigestValue);
                        } else {
                            logger.error("FormDigestValue element not found in XML response.");
                            throw new IOException("FormDigestValue element not found in XML response.");
                        }

                    } catch (ParserConfigurationException | SAXException e) {
                        logger.error("Error parsing XML response for FormDigestValue: {}", e.getMessage(), e);
                        logger.error("Problematic XML Body: {}", body); // Log the body again in case of parsing error
                        throw new IOException("Error parsing XML response for FormDigestValue", e);
                    }
                } else {
                    logger.error("Failed to retrieve FormDigestValue. Status Code: {}, Response Body: {}", statusCode, body);
                    throw new IOException("Failed to retrieve FormDigestValue. Status Code: " + statusCode);
                }
            } catch (ParseException e) {
                logger.error("Error parsing response for FormDigestValue: {}", e.getMessage(), e);
                throw new IOException("Error parsing response for FormDigestValue", e);
            }
        } catch (IOException e) {
            logger.error("Error during HTTP request for FormDigestValue: {}", e.getMessage(), e);
            throw e;
        }

        return formDigestValue;
    }
}