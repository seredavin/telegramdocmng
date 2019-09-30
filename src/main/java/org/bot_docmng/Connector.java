package org.bot_docmng;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class Connector {
    private final String apiURI = Setup.getInstance().getApiURI();
    private final String apiUser = Setup.getInstance().getApiUser();
    private final char[] apiPassword = Setup.getInstance().getApiPassword().toCharArray();

    private static String getParamsString(Map<String, String> params) {
        if (params == null) return "";
        StringBuilder result = new StringBuilder();
        result.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    static String postRequest(Map<String, String> requestText, String apiInterface)
            throws URISyntaxException, IOException, InterruptedException {
        Connector connector = new Connector();
        URI uri = new URI(connector.apiURI + "/"
                + apiInterface);
        return connector.postApiJson(uri, requestText);
    }

    static String getRequest(Map<String, String> requestText, String apiInterface)
            throws URISyntaxException, IOException, InterruptedException {
        Connector connector = new Connector();
        URI uri = new URI(connector.apiURI + "/"
                + apiInterface
                + getParamsString(requestText));
        return connector.getApiJson(uri, requestText);
    }

    private String postApiJson(URI uri, Map<String, String> map)
            throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(map);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                apiUser,
                                apiPassword
                        );
                    }
                })
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        String out = response.body();

        return out;
    }

    private String getApiJson(URI uri, Map<String, String> map)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                apiUser,
                                apiPassword
                        );
                    }
                })
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}