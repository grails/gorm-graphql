package gorm.graphql

import spock.lang.Specification
import spock.lang.Subject

class GraphQLRequestConstraintsSpec extends Specification {

    @Subject
    GraphQLRequest graphQLRequest = new GraphQLRequest()

    def "GraphQLExecutionRequest.query cannot be null"() {
        when:
        graphQLRequest.query = null

        then:
        !graphQLRequest.validate(['query'])
    }

    def "GraphQLExecutionRequest.operationName can be null"() {
        when:
        graphQLRequest.operationName = null

        then:
        graphQLRequest.validate(['operationName'])
    }

    def "GraphQLExecutionRequest.variables cannot be null"() {
        when:
        graphQLRequest.variables = null

        then:
        !graphQLRequest.validate(['variables'])
    }

    def "GraphQLExecutionRequest.variables can be an empty Map"() {
        when:
        graphQLRequest.variables = [:]

        then:
        graphQLRequest.validate(['variables'])
    }
}
