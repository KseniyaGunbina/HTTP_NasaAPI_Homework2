import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;



public class Main {
    public static final String REMOTE_SERVICE_URL = "https://api.nasa.gov/planetary/apod?api_key=le2MFCbzoKr1cqLuu0pfWuHywUxIOfm1wC3PLC8b";
    public static ObjectMapper mapper = new ObjectMapper();

    public static void printHeaders(CloseableHttpResponse response) {
        System.out.println("\nОтвет:\n");
        Arrays.stream(response.getAllHeaders())
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        try(CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setUserAgent("Get rating about cats")
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build()) {

            // создание объекта запроса NASA
            HttpGet request1 = new HttpGet(REMOTE_SERVICE_URL);
            request1.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

            // запрос по URL NASA
            CloseableHttpResponse response1 = httpClient.execute(request1);
            Main.printHeaders(response1);
            String body1 = EntityUtils.toString(response1.getEntity());

            // JSON  в объект
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            NasaAnswer nasaAnswer = gson.fromJson(body1, NasaAnswer.class);
            System.out.println("\nОбъект создан: " + nasaAnswer.toString());

            // запрос по URL изображения
            HttpGet request2 = new HttpGet(nasaAnswer.getUrl());
            CloseableHttpResponse response2 = httpClient.execute(request2);
            Main.printHeaders(response2);
            String body2 = new String(response2.getEntity()
                    .getContent()
                    .readAllBytes());

            // создание файла
            String filename = Arrays.stream(Arrays.stream(nasaAnswer.getUrl().split("/"))
                    .skip(6)
                    .findFirst()
                    .get()
                    .split("\\."))
                    .findFirst()
                    .get();

            byte[] bytes = body2.getBytes();
            Files.write(Path.of( "C:/Games/" + filename + ".txt"), bytes);

            // скачивание изображения
            InputStream in = new URL(nasaAnswer.getUrl()).openStream();
            Files.copy(in, Paths.get("C:/Games/" + filename + ".jpeg"), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
