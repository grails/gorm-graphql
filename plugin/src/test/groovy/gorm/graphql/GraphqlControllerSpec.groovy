package gorm.graphql

import grails.testing.web.controllers.ControllerUnitTest
import graphql.ExecutionResult
import graphql.GraphQL
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
        GrailsGraphQLConfiguration grailsGraphQLConfiguration = Stub(GrailsGraphQLConfiguration) {
            getEnabled() >> true
        }
        controller.grailsGraphQLConfiguration = grailsGraphQLConfiguration

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
        GrailsGraphQLConfiguration grailsGraphQLConfiguration = Stub(GrailsGraphQLConfiguration) {
            getEnabled() >> true
        }
        controller.grailsGraphQLConfiguration = grailsGraphQLConfiguration

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
        GrailsGraphQLConfiguration grailsGraphQLConfiguration = Stub(GrailsGraphQLConfiguration) {
            getEnabled() >> true
        }
        controller.grailsGraphQLConfiguration = grailsGraphQLConfiguration

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
        GrailsGraphQLConfiguration grailsGraphQLConfiguration = Stub(GrailsGraphQLConfiguration) {
            getEnabled() >> true
        }
        controller.grailsGraphQLConfiguration = grailsGraphQLConfiguration

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
        GrailsGraphQLConfiguration grailsGraphQLConfiguration = Stub(GrailsGraphQLConfiguration) {
            getEnabled() >> true
        }
        controller.grailsGraphQLConfiguration = grailsGraphQLConfiguration

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
        controller.grailsGraphQLConfiguration = new GrailsGraphQLConfiguration(browser: false)
        controller.browser()

        then:
        status == 404
    }

    void "test browser"() {
        when:
        controller.grailsGraphQLConfiguration = new GrailsGraphQLConfiguration(browser: true)
        controller.browser()

        then:
        !response.text.empty
        response.contentType == "text/html;charset=utf-8"
    }
}
