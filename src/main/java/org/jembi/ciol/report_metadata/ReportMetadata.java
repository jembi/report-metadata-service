package org.jembi.ciol.report_metadata;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ReportMetadata {
    private static final Logger LOGGER = LogManager.getLogger(ReportMetadata.class);

    private HttpServer httpServer;

    private ReportMetadata() {
        LOGGER.info("ReportMetadata started.");
    }

    public static void main(String[] args) {
        new ReportMetadata().run();
    }

    public Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    httpServer = new HttpServer();
                    httpServer.open(context.getSystem());
                    return Behaviors.receive(Void.class)
                                    .onSignal(Terminated.class,
                                              sig -> {
                                                  httpServer.close(context.getSystem());
                                                  return Behaviors.stopped();
                                              })
                                    .build();
                });
    }

    private void run() {
        ActorSystem.create(this.create(), "ReportMetaDataApp");
    }

}


