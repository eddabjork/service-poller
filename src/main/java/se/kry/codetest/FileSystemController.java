package se.kry.codetest;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static se.kry.codetest.MainVerticle.URL_NAME_KEY;
import static se.kry.codetest.MainVerticle.URL_STATUS_KEY;

public class FileSystemController {
    public static final String SERVICES_FILE_NAME = "services.txt";

    private FileSystem fileSystem;

    public FileSystemController(Vertx vertx) {
        fileSystem = vertx.fileSystem();
    }

    public void serviceFileExists(Handler<AsyncResult<Boolean>> resultHandler) {
        fileSystem.exists(SERVICES_FILE_NAME, resultHandler);
    }

    public void readServicesFileAndPumpTo(HashMap<String, String> services) {
        fileSystem.readFile(SERVICES_FILE_NAME, getReadHandler(services));
    }

    public void createServicesFile(Handler<AsyncResult<Void>> resultHandler) {
        fileSystem.createFile(SERVICES_FILE_NAME, resultHandler);
    }

    private Handler<AsyncResult<Buffer>> getReadHandler(HashMap<String, String> services) {
        return readResult -> {
            if (readResult.succeeded() && readResult.result() != null) {
                //only read from the file if it's not empty
                if (readResult.result().length() != 0) {
                    JsonArray saved = readResult.result().toJsonArray();
                    for (Object savedService : saved) {
                        JsonObject serviceJson = (JsonObject) savedService;
                        services.put(serviceJson.getValue(URL_NAME_KEY).toString(),
                                serviceJson.getValue(URL_STATUS_KEY).toString());
                    }
                }
            } else {
                System.err.println("failed to read file: " + readResult.cause().getMessage());
            }
        };
    }

    public void saveToServicesFile(Map<String, String> services) {
        fileSystem.open(SERVICES_FILE_NAME, new OpenOptions(), result -> {
            if (result.succeeded()) {
                AsyncFile file = result.result();
                List<JsonObject> jsonServices = services
                        .entrySet()
                        .stream()
                        .map(service ->
                                new JsonObject()
                                        .put(URL_NAME_KEY, service.getKey())
                                        .put(URL_STATUS_KEY, service.getValue()))
                        .collect(Collectors.toList());

                Buffer buff;
                if (jsonServices.isEmpty()) {
                    buff = Buffer.buffer("");
                } else {
                    buff = Buffer.buffer(jsonServices.toString());

                }

                file.write(buff);
            } else {
                System.err.println("Failed to open file");

            }
        });
    }
}