package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static se.kry.codetest.BackgroundPoller.STATUS_PENDING;

public class MainVerticle extends AbstractVerticle {
  public static final String URL_NAME_KEY = "name";
  public static final String URL_STATUS_KEY = "status";

  private static final int MILLISECONDS = 1000;
  private static final int POLLER_INTERVAL_SEC = 60;
  private static final int PORT = 8080;

  private static final String URL_REQUEST_KEY = "url";

  private HashMap<String, String> services = new HashMap<>();
  private BackgroundPoller poller = new BackgroundPoller();
  private FileSystemController fileSystemController;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);

    fileSystemController = new FileSystemController(vertx);

    fileSystemController.serviceFileExists(res -> {
      if (res.succeeded() && res.result()) {
        fileSystemController.readServicesFileAndPumpTo(services);
      } else {
        fileSystemController.createServicesFile(createRes -> {
          if (createRes.succeeded()) {
            System.out.println("services file created");
          } else {
            System.err.println("failed creating services file: " + createRes.cause().getMessage());
          }
        });
      }
    });
  }

  @Override
  public void start(Future<Void> startFuture) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    vertx.setPeriodic(MILLISECONDS * POLLER_INTERVAL_SEC, timerId ->
            poller.pollServices(services, vertx, fileSystemController));
    setRoutes(router);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(PORT, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void setRoutes(Router router){
    router.route("/").handler(StaticHandler.create());

    setGetServiceHandler(router);
    setPostServiceHandler(router);
    setPostDeleteServiceHandler(router);
  }

  private void setGetServiceHandler(Router router) {
    router.get("/service").handler(req -> {
      List<JsonObject> jsonServices = services
              .entrySet()
              .stream()
              .map(service ->
                      new JsonObject()
                              .put(URL_NAME_KEY, service.getKey())
                              .put(URL_STATUS_KEY, service.getValue()))
              .collect(Collectors.toList());
      req.response()
              .putHeader("content-type", "application/json")
              .end(new JsonArray(jsonServices).encode());
    });
  }

  private void setPostServiceHandler(Router router) {
    router.post("/service").handler(req -> {
      String url = getUrlFromRequestBody(req);
      services.put(url, STATUS_PENDING);
      responseRequestWithTextOK(req);
      poller.pollServices(services, vertx, fileSystemController);
    });
  }

  private void setPostDeleteServiceHandler(Router router) {
    router.post("/delete").handler(req -> {
      String url = getUrlFromRequestBody(req);
      services.remove(url);
      responseRequestWithTextOK(req);
      fileSystemController.saveToServicesFile(services);
    });
  }

  private String getUrlFromRequestBody(RoutingContext request) {
    JsonObject jsonBody = request.getBodyAsJson();
    return jsonBody.getString(URL_REQUEST_KEY);
  }

  private void responseRequestWithTextOK(RoutingContext request) {
    request.response()
            .putHeader("content-type", "text/plain")
            .end("OK");
  }
}



