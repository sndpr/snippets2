package org.psc.resteasy;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ClientApplication {

    public static void main(String[] args) {
        ClientRequestFilter requestFilter =
                requestContext -> requestContext.getHeaders().add("fromFilterKey", "fromFilterValue");
        Client client = ResteasyClientBuilder.newClient().register(requestFilter);
        WebTarget webTarget = client.target("http://localhost:8080/api/service/misc/echo?value=abc");

        //noinspection unchecked
        Map<String, Object> response = webTarget.request().header("testKey", "testValue").get(Map.class);

        log.info(response.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " : " + entry.getValue())
                .collect(Collectors.joining(", ")));
    }
}