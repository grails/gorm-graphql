package gorm.graphql

import grails.testing.web.controllers.ControllerUnitTest
import grails.util.TypeConvertingMap
import graphql.ExecutionResult
import graphql.GraphQL
import groovy.json.JsonSlurper
import spock.lang.Specification

class GraphqlControllerSpec extends Specification implements ControllerUnitTest<GraphqlController> {

    def setup() {
    }

    def cleanup() {
    }

    private ExecutionResult mockExecutionResult() {
        Mock(ExecutionResult) {
            1 * getErrors() >> []
            1 * getData() >> ''
        }
    }

    void "test graphql with GET request"() {
        given:
        GraphQL graphQL = Mock(GraphQL)
        controller.graphQL = graphQL

        when:
        params.query = 'query'
        controller.index()

        then:
        1 * graphQL.execute('query', null, [locale: request.locale], Collections.emptyMap()) >> mockExecutionResult()

        when:
        params.query = 'query2'
        params.operationName = 'operationName'
        controller.index()

        then:
        1 * graphQL.execute('query2', 'operationName', [locale: request.locale], Collections.emptyMap()) >> mockExecutionResult()

        when:
        params.query = 'query2'
        params.operationName = 'operationName'
        params.variables = '{"foo": 2}'
        controller.index()

        then:
        1 * graphQL.execute('query2', 'operationName', [locale: request.locale], [foo: 2]) >> mockExecutionResult()
    }

    void "test graphql with POST body application/json (query only)"() {
        given:
        GraphQL graphQL = Mock(GraphQL)
        controller.graphQL = graphQL

        when:
        request.setJson('{"query": "query"}')
        request.method = 'POST'
        controller.index()

        then:
        1 * graphQL.execute('query', null, [locale: request.locale], Collections.emptyMap()) >> mockExecutionResult()
    }

    void "test graphql with POST body application/json (query and operationName)"() {
        given:
        GraphQL graphQL = Mock(GraphQL)
        controller.graphQL = graphQL

        when:
        request.setJson('{"query": "query2", "operationName": "operationName"}')
        request.method = 'POST'
        controller.index()

        then:
        1 * graphQL.execute('query2', 'operationName', [locale: request.locale], Collections.emptyMap()) >> mockExecutionResult()
    }

    void "test graphql with POST body application/json (all data)"() {
        given:
        GraphQL graphQL = Mock(GraphQL)
        controller.graphQL = graphQL

        when:
        request.setJson('{"query": "query2", "operationName": "operationName", "variables": {"foo": 2}}')
        request.method = 'POST'
        controller.index()

        then:
        1 * graphQL.execute('query2', 'operationName', [locale: request.locale], [foo: 2]) >> mockExecutionResult()
    }

    void "test graphql with POST body application/graphql"() {
        given:
        GraphQL graphQL = Mock(GraphQL)
        controller.graphQL = graphQL

        when:
        request.setJson('{"query": "query"}')
        request.setContentType("application/graphql; charset=UTF-8")
        request.method = 'POST'
        controller.index()

        then:
        1 * graphQL.execute('{"query": "query"}', null, [locale: request.locale], Collections.emptyMap()) >> mockExecutionResult()
    }

    void "test browser when disabled"() {
        when:
        controller.browserEnabled = false
        controller.browser()

        then:
        status == 404
    }

    void "test browser"() {
        when:
        controller.browserEnabled = true
        controller.browser()

        then:
        !response.text.empty
        response.contentType == "text/html;charset=utf-8"
    }
}
