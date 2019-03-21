package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;

public class BackgroundPoller {
  public static final String STATUS_PENDING = "pending...";

  private final String STATUS_FAIL = "FAIL";
  private final String STATUS_OK = "OK";
  private final int STATUS_CODE_OK = 200;

  public Future<List<String>> pollServices(Map<String, String> services, Vertx vertx,
                                           FileSystemController fileSystemController) {
    services.forEach((url, status) -> {
      WebClient.create(vertx).getAbs(url).send(result -> {
        if (result == null || result.result() == null) {
          services.put(url, STATUS_FAIL);
          fileSystemController.saveToServicesFile(services);
          return;
        }

        if (result.result().statusCode() == STATUS_CODE_OK) {
          services.put(url, STATUS_OK);
        } else {
          services.put(url, STATUS_FAIL);
        }
        fileSystemController.saveToServicesFile(services);
      });
    });
    return Future.succeededFuture();
  }
}
