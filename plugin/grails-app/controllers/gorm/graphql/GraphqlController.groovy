package gorm.graphql

import grails.io.IOUtils
import grails.util.TypeConvertingMap
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import groovy.json.JsonSlurper
import org.springframework.http.HttpMethod

class GraphqlController {

    GraphQLSchema graphQLSchema

    protected Object buildContext() {
        [locale: request.locale]
    }

    def index() {
        def graphql = new GraphQL(graphQLSchema)

        String query = null
        String operationName = null
        Object context = buildContext()
        Map<String, Object> variables = null

        HttpMethod method = HttpMethod.resolve(request.method)
        if (request.contentLength != 0 && method != HttpMethod.GET) {

            String encoding = request.characterEncoding
            if (encoding == null) {
                encoding = "UTF-8"
            }

            String body = IOUtils.toString(request.inputStream, encoding)

            if (request.contentType == "application/json") {
                TypeConvertingMap json = new TypeConvertingMap((Map)new JsonSlurper().parseText(body))
                query = json.query.toString()
                operationName = json.containsKey('operationName') ? operationName : null
                if (json.variables instanceof Map) {
                    variables = json.variables
                } else {
                    variables = Collections.emptyMap()
                }
            }
            else if (request.contentType == "application/graphql") {
                query = body
                operationName = null
                variables = Collections.emptyMap()
            }
        } else {
            query = params.query
            operationName = params.containsKey('operationName') ? params.operationName : null
            if (params.containsKey('variables')) {
                variables = (Map)new JsonSlurper().parseText(params.variables)
            } else {
                variables = Collections.emptyMap()
            }
        }

        ExecutionResult executionResult = graphql.execute(query, operationName, context, variables)
        Map<String, Object> result = new LinkedHashMap<>()
        if (executionResult.getErrors().size() > 0) {
            result.put("errors", executionResult.getErrors())
        }
        result.put("data", executionResult.getData())

        respond result
    }
}
