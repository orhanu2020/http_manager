package org.du.http;



import lombok.Getter;
import okhttp3.*;
import org.du.network.crypto.CryptoPackage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class HttpRest {
    private OkHttpClient httpClient;

    @Getter
    private String authorizationCode;
    public HttpRest() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            OkHttpClient.Builder newBuilder = new OkHttpClient.Builder();
            newBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            newBuilder.hostnameVerifier((hostname, session) -> true);

            this.httpClient = newBuilder
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            this.authorizationCode = "";
        } catch (Exception e) {}
    }

    public void authorize(String  credentialManagerServiceUrl,
                          CryptoPackage requestPackage,
                          String sourceServiceName)
            throws IOException
    {
        try {
            CryptoPackage authRequest = CryptoPackage.pack(requestPackage.getData(), sourceServiceName);

            RequestBody requestBody = RequestBody
                    .create(MediaType.parse("application/json"), authRequest.toString());
            Request request = new Request.Builder()
                    .url(credentialManagerServiceUrl)
                    .post(requestBody)
                    .build();

            try(Response response = httpClient.newCall(request).execute()) {
                assert response.body() != null;
                CryptoPackage responsePacket = CryptoPackage.parseJson(response.body().string());
                responsePacket.verify();
                this.authorizationCode = responsePacket.getData();
            }
        } catch (IOException e) {
            this.authorizationCode = "";
            throw e;
        }
    }

    public CryptoPackage post(String toUrl, CryptoPackage cryptoPackage) throws IOException {
        RequestBody requestBody =
                RequestBody.create(
                        cryptoPackage.toString().getBytes(StandardCharsets.UTF_8),
                        MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(toUrl)
                .post(requestBody)
                .build();
        try(Response response = httpClient.newCall(request).execute()){
            assert response.body() != null;
            String resultJson = response.body().string();
            return CryptoPackage.parseJson(resultJson);
        }
    }

    public String post(String toUrl, String body) throws IOException {
        RequestBody requestBody =
                RequestBody.create(
                        body.getBytes(StandardCharsets.UTF_8),
                        MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(toUrl)
                .post(requestBody)
                .build();
        try(Response response = httpClient.newCall(request).execute()){
            assert response.body() != null;
            String resultJson = response.body().string();
            return resultJson;
        }
    }

    public HttpResponse post(String toUrl, Map<String, String> headerMap, String body) throws IOException {

        RequestBody requestBody =
                RequestBody.create(
                        body.getBytes(StandardCharsets.UTF_8),
                        MediaType.get("application/json"));

        Headers headers = Headers.of(headerMap);
        Request request = new Request.Builder()
                .url(toUrl)
                .post(requestBody)
                .headers(headers)
                .build();
        HttpResponse httpResponse = new HttpResponse();
        try(Response response = httpClient.newCall(request).execute()){
            httpResponse.setSuccess(response.isSuccessful());
            httpResponse.setData(response.body().string());
            return httpResponse;
        }
    }

    public String get(String toUrl, Map<String, String> headerMap)
            throws IOException
    {
        Headers headers = Headers.of(headerMap);
        Request request = new Request.Builder()
                .url(toUrl)
                .headers(headers)
                .get()
                .build();
        try(Response response = httpClient.newCall(request).execute()){
            assert response.body() != null;
            String resultData = response.body().string();
            return resultData;
        }
    }
}
