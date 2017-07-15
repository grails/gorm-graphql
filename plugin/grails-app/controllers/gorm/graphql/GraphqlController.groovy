package gorm.graphql

import grails.io.IOUtils
import grails.util.TypeConvertingMap
import grails.web.mapping.LinkGenerator
import grails.web.mime.MimeType
import graphql.ExecutionResult
import graphql.GraphQL
import groovy.json.JsonSlurper
import org.springframework.http.HttpMethod

class GraphqlController {

    static responseFormats = ['json', 'xml']

    public static MimeType GRAPHQL =  new MimeType('application/graphql')

    GraphQL graphQL

    LinkGenerator grailsLinkGenerator

    GrailsGraphQLConfiguration grailsGraphQLConfiguration

    protected Object buildContext() {
        [locale: request.locale]
    }

    def index() {
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

            if (request.mimeTypes.contains(MimeType.JSON)) {
                TypeConvertingMap json = new TypeConvertingMap((Map)new JsonSlurper().parseText(body))
                query = json.query.toString()
                operationName = json.containsKey('operationName') ? json.operationName : null
                if (json.variables instanceof Map) {
                    variables = json.variables
                } else {
                    variables = Collections.emptyMap()
                }
            }
            else if (request.mimeTypes.contains(GRAPHQL)) {
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

        ExecutionResult executionResult = graphQL.execute(query, operationName, context, variables)
        Map<String, Object> result = new LinkedHashMap<>()
        if (executionResult.getErrors().size() > 0) {
            result.put("errors", executionResult.getErrors())
        }
        result.put("data", executionResult.getData())

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
