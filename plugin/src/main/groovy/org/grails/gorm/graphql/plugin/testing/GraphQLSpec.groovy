package org.grails.gorm.graphql.plugin.testing

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.json.StreamingJsonBuilder
import groovy.transform.TupleConstructor
import org.springframework.beans.factory.annotation.Value

trait GraphQLSpec {

    private static RestBuilder _rest
    private static String _url
    private static GraphQLRequestHelper _graphql

    @Value('${local.server.port}')
    String serverPort

    GraphQLRequestHelper getGraphQL() {
        if (_graphql == null) {
            _graphql = new GraphQLRequestHelper(getRest(), getUrl())
        }
        _graphql
    }

    RestBuilder getRest() {
        if (_rest == null) {
            _rest = new RestBuilder()
        }
        _rest
    }

    String getUrl() {
        if (_url == null) {
            _url = "http://localhost:${serverPort}/graphql"
        }
        _url
    }

    @TupleConstructor
    static class GraphQLRequestHelper {

        RestBuilder rest
        String url

        RestResponse graphql(String requestBody) {
            rest.post(url) {
                contentType('application/graphql')
                body(requestBody)
            }
        }

        private RestResponse buildJsonRequest(Map data) {
            StringWriter sw = new StringWriter()
            if (data.containsKey('variables')) {
                sw = new StringWriter()
                new StreamingJsonBuilder(sw).call(data.variables)
                data.put('variables', sw.toString())
            }
            sw = new StringWriter()
            new StreamingJsonBuilder(sw).call(data)

            rest.post(url) {
                json(sw.toString())
            }
        }
        private RestResponse buildGetRequest(Map data) {
            if (data.containsKey('variables')) {
                StringWriter sw = new StringWriter()
                new StreamingJsonBuilder(sw).call(data.variables)
                data.put('variables', sw.toString())
            }

            rest.get(url, data)
        }
        
        RestResponse json(String query) {
            buildJsonRequest([query: query])
        }
        RestResponse json(String query, String operationName) {
            buildJsonRequest([query: query, operationName: operationName])
        }
        RestResponse json(String query, Map variables) {
            buildJsonRequest([query: query, variables: variables])
        }
        RestResponse json(String query, Map variables, String operationName) {
            buildJsonRequest([query: query, operationName: operationName, variables: variables])
        }

        RestResponse get(String query) {
            buildGetRequest([query: query])
        }
        RestResponse get(String query, String operationName) {
            buildGetRequest([query: query, operationName: operationName])
        }
        RestResponse get(String query, Map variables) {
            buildGetRequest([query: query, variables: variables])
        }
        RestResponse get(String query, Map variables, String operationName) {
            buildGetRequest([query: query, operationName: operationName, variables: variables])
        }
    }

}
