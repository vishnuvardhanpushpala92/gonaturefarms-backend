package com.gonaturefarms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonaturefarms.config.WhatsAppConfig;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class WhatsAppApiService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppApiService.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final WhatsAppConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WhatsAppApiService(WhatsAppConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();

        // Configure HTTP client with timeouts
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Upload an image to WhatsApp servers and return the media ID
     */
    public String uploadMedia(String imageUrl) throws IOException {
        if (!config.isConfigured()) {
            throw new IllegalStateException("WhatsApp API is not configured. Please set access token and phone number ID.");
        }

        // Download the image from the public URL
        if (imageUrl == null || !imageUrl.startsWith("http")) {
            throw new IllegalArgumentException("Image URL must be a public HTTPS URL");
        }

        logger.info("Downloading image from: {}", imageUrl);

        // Download the image
        Request downloadRequest = new Request.Builder()
                .url(imageUrl)
                .get()
                .build();

        try (Response downloadResponse = httpClient.newCall(downloadRequest).execute()) {
            if (!downloadResponse.isSuccessful()) {
                throw new IOException("Failed to download image: " + downloadResponse.code());
            }

            byte[] imageBytes = downloadResponse.body() != null ? downloadResponse.body().bytes() : null;
            if (imageBytes == null || imageBytes.length == 0) {
                throw new IOException("Downloaded image is empty");
            }

            logger.info("Image downloaded successfully, size: {} bytes", imageBytes.length);

            // Upload to WhatsApp Media API
            String uploadUrl = config.getApiUrl() + "/" + config.getPhoneNumberId() + "/media";

            // Determine content type (default to image/png as requested)
            MediaType mediaType = MediaType.parse("image/png");

            RequestBody imageBody = RequestBody.create(imageBytes, mediaType);
            MultipartBody uploadBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "product_image.png", imageBody)
                    .addFormDataPart("messaging_product", "whatsapp")
                    .addFormDataPart("type", "image/png")
                    .build();

            Request uploadRequest = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "Bearer " + config.getAccessToken())
                    .post(uploadBody)
                    .build();

            try (Response uploadResponse = httpClient.newCall(uploadRequest).execute()) {
                String responseBody = uploadResponse.body() != null ? uploadResponse.body().string() : "";
                logger.info("WhatsApp Media API response: {}", responseBody);

                if (!uploadResponse.isSuccessful()) {
                    logger.error("WhatsApp Media API error: {} - {}", uploadResponse.code(), responseBody);
                    throw new IOException("WhatsApp Media API error: " + uploadResponse.code() + " - " + responseBody);
                }

                // Extract media ID from response
                JsonNode responseJson = objectMapper.readTree(responseBody);
                String mediaId = responseJson.path("id").asText();

                if (mediaId == null || mediaId.isEmpty()) {
                    throw new IOException("Media ID not found in response");
                }

                logger.info("Image uploaded successfully, media ID: {}", mediaId);
                return mediaId;
            }
        }
    }

    /**
     * Send an image message with caption to a phone number using media ID
     */
    public JsonNode sendImageMessage(String toPhone, String mediaId, String caption) throws IOException {
        if (!config.isConfigured()) {
            throw new IllegalStateException("WhatsApp API is not configured. Please set access token and phone number ID.");
        }

        String url = config.getApiUrl() + "/" + config.getPhoneNumberId() + "/messages";

        // Build the message payload using media ID
        String payload = buildImageMessagePayload(toPhone, mediaId, caption);

        RequestBody body = RequestBody.create(payload, JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + config.getAccessToken())
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            logger.info("WhatsApp API response: {}", responseBody);

            if (!response.isSuccessful()) {
                logger.error("WhatsApp API error: {} - {}", response.code(), responseBody);
                throw new IOException("WhatsApp API error: " + response.code() + " - " + responseBody);
            }

            return objectMapper.readTree(responseBody);
        }
    }

    /**
     * Send a template message with image header
     */
    public JsonNode sendTemplateMessage(String toPhone, String mediaUrl, String productName, String price, String mrp, String productUrl) throws IOException {
        if (!config.isConfigured()) {
            throw new IllegalStateException("WhatsApp API is not configured. Please set access token and phone number ID.");
        }

        String url = config.getApiUrl() + "/" + config.getPhoneNumberId() + "/messages";

        // Build the template message payload
        String payload = buildTemplateMessagePayload(toPhone, mediaUrl, productName, price, mrp, productUrl);

        RequestBody body = RequestBody.create(payload, JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + config.getAccessToken())
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            logger.info("WhatsApp API response: {}", responseBody);

            if (!response.isSuccessful()) {
                logger.error("WhatsApp API error: {} - {}", response.code(), responseBody);
                throw new IOException("WhatsApp API error: " + response.code() + " - " + responseBody);
            }

            return objectMapper.readTree(responseBody);
        }
    }

    /**
     * Build image message payload using media ID
     */
    private String buildImageMessagePayload(String toPhone, String mediaId, String caption) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"messaging_product\": \"whatsapp\",");
        sb.append("\"to\": \"").append(toPhone).append("\",");
        sb.append("\"type\": \"image\",");
        sb.append("\"image\": {");
        sb.append("\"id\": \"").append(mediaId).append("\",");
        if (caption != null && !caption.isEmpty()) {
            sb.append("\"caption\": \"").append(escapeJson(caption)).append("\"");
        }
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Build template message payload with dynamic parameters
     */
    private String buildTemplateMessagePayload(String toPhone, String mediaUrl, String productName, String price, String mrp, String productUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"messaging_product\": \"whatsapp\",");
        sb.append("\"to\": \"").append(toPhone).append("\",");
        sb.append("\"type\": \"template\",");
        sb.append("\"template\": {");
        sb.append("\"name\": \"").append(config.getTemplateName()).append("\",");
        sb.append("\"language\": {\"code\": \"").append(config.getTemplateLanguage()).append("\"},");

        // Template components with dynamic parameters
        sb.append("\"components\": [");

        // Header with image
        sb.append("{");
        sb.append("\"type\": \"header\",");
        sb.append("\"parameters\": [");
        sb.append("{");
        sb.append("\"type\": \"image\",");
        sb.append("\"image\": {");
        sb.append("\"link\": \"").append(mediaUrl).append("\"");
        sb.append("}");
        sb.append("}");
        sb.append("]");
        sb.append("},");

        // Body with product details
        sb.append("{");
        sb.append("\"type\": \"body\",");
        sb.append("\"parameters\": [");
        sb.append("{\"type\": \"text\", \"text\": \"").append(escapeJson(productName)).append("\"},");
        sb.append("{\"type\": \"text\", \"text\": \"").append(escapeJson(price)).append("\"},");
        if (mrp != null && !mrp.isEmpty()) {
            sb.append("{\"type\": \"text\", \"text\": \"").append(escapeJson(mrp)).append("\"},");
        }
        sb.append("{\"type\": \"text\", \"text\": \"").append(escapeJson(productUrl)).append("\"}");
        sb.append("]");
        sb.append("}");

        sb.append("]");
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Normalize phone number for WhatsApp API (remove non-digits, ensure country code)
     */
    public String normalizePhoneNumber(String phone) {
        if (phone == null) return null;

        // Remove all non-digit characters
        String normalized = phone.replaceAll("[^0-9]", "");

        // Add country code if missing (assuming India: 91)
        if (normalized.length() == 10 && !normalized.startsWith("91")) {
            normalized = "91" + normalized;
        }

        return normalized;
    }
}
