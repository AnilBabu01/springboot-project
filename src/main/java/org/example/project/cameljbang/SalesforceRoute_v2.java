package org.example.project.cameljbang;

import org.apache.camel.builder.RouteBuilder;

import org.springframework.stereotype.Component;

@Component
public class SalesforceRoute_v2 extends RouteBuilder {

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
        // 1. GET ACCOUNTS
        // GET /accounts
        // =========================================================

        from("platform-http:/accounts?httpMethodRestrict=GET")
                .routeId("Salesforce-Get-Accounts")

                .log("GET /accounts request received")

                .process(exchange -> {

                    String query =
                            "SELECT Id, Name, Website, Type, Description " +
                            "FROM Account ";

                    exchange.getMessage().setHeader("q", query);
                })

                .log("Fetching Accounts from Salesforce...")

                .to("salesforce:raw"
                        + "?format=JSON"
                        + "&rawMethod=GET"
                        + "&rawQueryParameters=q"
                        + "&maxRecords=100"
                        + "&rawPath=/services/data/v60.0/query")

                .setHeader("Content-Type", constant("application/json"))

                .log("Salesforce GET response: ${body}");


        // =========================================================
        // 2. CREATE ACCOUNT
        // POST /accounts
        // =========================================================

        from("platform-http:/accounts?httpMethodRestrict=POST")
                .routeId("Salesforce-Create-Account")

                .log("POST /accounts request received")

                .log("Account request body: ${body}")

                .setHeader("Content-Type", constant("application/json"))

                .to("salesforce:raw"
                        + "?format=JSON"
                        + "&rawMethod=POST"
                        + "&rawPath=/services/data/v60.0/sobjects/Account")

                .setHeader("Content-Type", constant("application/json"))

                .log("Account created successfully")

                .log("Salesforce CREATE response: ${body}");


    }
}
