package com.gabe.animalia.ml.server;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

public class CloudWatchLogger {
        // Use the Async client to prevent blocking the game loop
        private static final CloudWatchAsyncClient cwClient = CloudWatchAsyncClient.builder()
                        .region(Region.US_EAST_2) // Replace with your actual region, e.g., CA_CENTRAL_1
                        .build();

        public static void logMetric(String metricName, double value, String unit) {
                Dimension dimension = Dimension.builder()
                                .name("Project")
                                .value("Wilderchess")
                                .build();

                MetricDatum datum = MetricDatum.builder()
                                .metricName(metricName)
                                .unit(unit) // e.g., "Milliseconds"
                                .value(value)
                                .dimensions(dimension)
                                .build();

                PutMetricDataRequest request = PutMetricDataRequest.builder()
                                .namespace("Wilderchess/Inference")
                                .metricData(datum)
                                .build();

                // This returns a CompletableFuture; we don't need to join()
                // because we want it to run in the background.
                cwClient.putMetricData(request);
        }
}
