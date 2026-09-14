package org.example.project.cameljbang;

import org.apache.camel.builder.RouteBuilder;

import org.springframework.stereotype.Component;

@Component
public class SalesforceRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // =========================================================
        // GLOBAL ERROR HANDLER
        // =========================================================

        onException(Exception.class)
                .handled(true)
                .log("Salesforce Operation Error: ${exception.message}")
                .setHeader("Content-Type", constant("application/json"));


      

        // =========================================================
        // 3. DELETE ACCOUNT
        // DELETE /accounts/{id}
        // =========================================================

        from("platform-http:/accounts/{id}?httpMethodRestrict=DELETE")
                .routeId("Salesforce-Delete-Account")

                .log("DELETE /accounts/${header.id} request received")

                .process(exchange -> {

                    String accountId =
                            exchange.getMessage().getHeader("id", String.class);

                    if (accountId == null || accountId.isBlank()) {
                        throw new IllegalArgumentException(
                                "Account ID is required"
                        );
                    }

                    exchange.getMessage().setHeader(
                            "accountId",
                            accountId
                    );
                })

                .log("Deleting Salesforce Account: ${header.accountId}")

                .toD("salesforce:raw"
                        + "?format=JSON"
                        + "&rawMethod=DELETE"
                        + "&rawPath=/services/data/v60.0/sobjects/Account/${header.accountId}")

                .setHeader("Content-Type", constant("application/json"))

                .setBody(constant(
                        "{\"success\":true,\"message\":\"Account deleted successfully\"}"
                ))

                .log("Salesforce Account deleted successfully");


        // =========================================================
        // 4. TEST SALESFORCE CONNECTION
        // =========================================================

        from("timer:salesforceTimer?repeatCount=1&delay=3000")
                .routeId("Salesforce-Query-Route")

                .log("Connecting to Salesforce...")

                .process(exchange -> {

                    String query =
                            "SELECT Id, Name, Website, Type, Description " +
                            "FROM Account " +
                            "LIMIT 5";

                    exchange.getMessage().setHeader("q", query);
                })

                .to("salesforce:raw"
                        + "?format=JSON"
                        + "&rawMethod=GET"
                        + "&rawQueryParameters=q"
                        + "&maxRecords=100"
                        + "&rawPath=/services/data/v60.0/query")

                .log("Salesforce timer query completed")
                .log("Response Body: ${body}");
    }
}
