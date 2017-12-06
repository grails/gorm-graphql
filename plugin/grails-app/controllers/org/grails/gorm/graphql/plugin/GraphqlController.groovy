package org.grails.gorm.graphql.plugin

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

    GraphQLContextBuilder graphQLContextBuilder

    def index() {
        if (!grailsGraphQLConfiguration.enabled) {
            render(status: 404)
            return
        }

        GraphQLRequests graphQLRequests

        HttpMethod method = HttpMethod.resolve(request.method)
        if (request.contentLength != 0 && method != HttpMethod.GET) {
            String encoding = request.characterEncoding ?: 'UTF-8'
            String body = IOUtils.toString(request.inputStream, encoding)
            graphQLRequests = GraphQLRequestUtils.graphQLRequestWithBodyAndMimeTypes(body, request.mimeTypes)
        } else {
            graphQLRequests = GraphQLRequestUtils.graphQLRequestWithParams(params)
        }

        if (!graphQLRequests.validate()) {
            String message = messageSource.getMessage('graphql.invalid.request', [] as Object[], 'Invalid GraphQL request', request.locale)
            render view: '/graphql/invalidRequest', model: [error: message]
            return
        }        
                
        while(graphQLRequests.hasNext()){
            GraphQLRequest graphQLRequest = graphQLRequests.next()
            
            Object context = graphQLContextBuilder.buildContext(currentRequestAttributes())

            ExecutionResult executionResult = graphQL.execute(graphQLRequest.query,
                graphQLRequest.operationName,
                context,
                graphQLRequest.variables)
            graphQLRequests.setResult(executionResult)
        }
        
        render view: graphQLRequests.view,model: graphQLRequests.model
    }

    private String resolvedBrowserHtml

    def browser() {
        if (grailsGraphQLConfiguration.enabled && grailsGraphQLConfiguration.browser) {
            if (resolvedBrowserHtml == null) {
                String endpoint = grailsLinkGenerator.link(controller: 'graphql', action: 'index')
                String staticBase = grailsLinkGenerator.resource([:])

                if (!staticBase.endsWith('/')) {
                    staticBase = staticBase + '/'
                }

                resolvedBrowserHtml = IOUtils.toString(this.class.classLoader.getResourceAsStream('graphiql.html'), "UTF8")
                        .replaceAll(/\{endpoint}/, endpoint)
                        .replaceAll(/\{staticBase}/, staticBase)
            }

            render(text: resolvedBrowserHtml, contentType: 'text/html')
        } else {
            render(status: 404)
        }
    }
}
