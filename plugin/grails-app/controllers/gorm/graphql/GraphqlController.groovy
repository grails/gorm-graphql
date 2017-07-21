package gorm.graphql

import grails.io.IOUtils
import grails.web.mapping.LinkGenerator
import graphql.ExecutionResult
import graphql.GraphQL
import groovy.transform.CompileStatic
import org.springframework.context.MessageSource
import org.springframework.http.HttpMethod

@CompileStatic
class GraphqlController {

    static responseFormats = ['json', 'xml']

    GraphQL graphQL

    LinkGenerator grailsLinkGenerator

    GrailsGraphQLConfiguration grailsGraphQLConfiguration

    MessageSource messageSource

    protected Object buildContext() {
        [locale: request.locale]
    }

    def index() {
        if (!grailsGraphQLConfiguration.enabled) {
            render(status: 404)
            return
        }

        GraphQLRequest graphQLRequest

        HttpMethod method = HttpMethod.resolve(request.method)
        if (request.contentLength != 0 && method != HttpMethod.GET) {
            String encoding = request.characterEncoding ?: 'UTF-8'
            String body = IOUtils.toString(request.inputStream, encoding)
            graphQLRequest = GraphQLRequestUtils.graphQLRequestWithBodyAndMimeTypes(body, request.mimeTypes)
        } else {
            graphQLRequest = GraphQLRequestUtils.graphQLRequestWithParams(params)
        }

        Map<String, Object> result = new LinkedHashMap<>()
        if (graphQLRequest == null) {
            response.setStatus(422)
            return result
        }
        if ( !graphQLRequest.validate() ) {
            result.put('errors', graphQLRequest.graphQLErrors(messageSource, request.locale))
            response.setStatus(422)
            return result
        }

        Object context = buildContext()
        ExecutionResult executionResult = graphQL.execute(graphQLRequest.query, graphQLRequest.operationName, context, graphQLRequest.variables)
        if (executionResult.errors.size() > 0) {
            result.put('errors', executionResult.errors)
        }
        result.put('data', executionResult.data)
        result
    }

    def browser() {
        if (grailsGraphQLConfiguration.browser) {
            String endpoint = grailsLinkGenerator.link(controller: 'graphql', action: 'index')
            String staticBase = grailsLinkGenerator.resource([:])

            if (!staticBase.endsWith('/')) {
                staticBase = staticBase + '/'
            }

            String html = IOUtils.toString(this.class.classLoader.getResourceAsStream('graphiql.html'), "UTF8")
                    .replaceAll(/\{endpoint}/, endpoint)
                    .replaceAll(/\{staticBase}/, staticBase)

            render(text: html, contentType: 'text/html')
        } else {
            render(status: 404)
        }
    }
}
