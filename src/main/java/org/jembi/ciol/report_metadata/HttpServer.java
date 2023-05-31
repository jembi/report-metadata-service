package org.jembi.ciol.report_metadata;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import com.fasterxml.jackson.core.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.ciol.MyConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;


public class HttpServer extends AllDirectives {

    private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);
    private CompletionStage<ServerBinding> binding = null;

    List<String> newMetadata = new ArrayList<>();
    List<String> currMetadata = new ArrayList<>();

    private HttpResponse isValidJson(String jsonData) {
        try {
            String currMetadataConfig = MetadataValidation.readJsonFile("/app/metadata/report_metadata.json");
            newMetadata = MetadataValidation.getKeysInJson(jsonData);
            currMetadata = MetadataValidation.getKeysInJson(currMetadataConfig);

            List<String> diff = new ArrayList<>(currMetadata);
            diff.removeAll(newMetadata);

            if (!diff.isEmpty()){
                LOGGER.debug("Validation failed due to missing fields:" + diff + " " + "in metadata config");
                return HttpResponse.create()
                        .withStatus(StatusCodes.BAD_REQUEST)
                        .withEntity("Missing fields in metadata config file");
            }
            LOGGER.debug("Validation Successful...");
            File myObj = new File("/app/metadata/report_metadata.json");
            myObj.delete();
            writeJsonToFile(jsonData);

        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return HttpResponse.create().withStatus(StatusCodes.OK);
    }

    public void writeJsonToFile(String jsonStr) {

        try {
            FileWriter file = new FileWriter("/app/metadata/report_metadata.json");
            file.write(jsonStr);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(jsonStr);
        LOGGER.info("File created");

    }

    void close(ActorSystem<Void> system) {
        binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
               .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    void open(final ActorSystem<Void> system) {
        ActorSystem.create(Behaviors.empty(), "routes");
        final Http http = Http.get(system);
        HttpServer app = new HttpServer();
        binding = http.newServerAt(MyConfig.HTTP_SERVER_HOST, MyConfig.HTTP_SERVER_PORT)
                      .bind(app.createRoute());
        LOGGER.info("Server online at http://{}:{}", MyConfig.HTTP_SERVER_HOST, MyConfig.HTTP_SERVER_PORT);
    }
    // /app/metadata/report_metadata.json
    private Route createRoute() {
        final var settings = CorsSettings.defaultSettings().withAllowGenericHttpRequests(true);
        return cors(settings,
                () -> pathPrefix("metadata",
                        () -> concat(
                                post(() -> concat(
                                        path("newConfig",
                                                () -> {
                                                    LOGGER.debug("newConfig");
                                                    return entity(
                                                            Unmarshaller.entityToString(),
                                                            (json) -> completeWithFuture(
                                                                    CompletableFuture.supplyAsync(
                                                                            () -> isValidJson(json)
                                                                    )
                                                            )
                                                    );
                                                }
                                        )
                                )),
                                get(() -> concat(
                                        path("currConfig",
                                                () -> {
                                                    final String jsonStr = MetadataValidation.readJsonFile(
                                                            "/app/metadata/report_metadata.json");
                                                    return complete(jsonStr);
                                                }
                                        )
                                ))
                        ))
        );
    }

}
