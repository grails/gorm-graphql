package gorm.graphql

import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable
import graphql.ErrorType
import graphql.GraphQL
import graphql.GraphQLError
import org.springframework.context.MessageSource
import org.springframework.validation.ObjectError

@GrailsCompileStatic
class GraphQLRequest implements Validateable {
    String query
    String operationName
    Map<String, Object> variables = [:]

    static constraints = {
        query nullable: false
        operationName nullable: true
        variables nullable: false
    }

    List<GraphQLError> graphQLErrors(MessageSource messageSource, Locale locale) {
        errors.allErrors.collect { ObjectError error ->
            String message = messageSource.getMessage(error.code, error.arguments, error.defaultMessage, locale)
            new GraphQLErrorImpl(message: message, errorType: ErrorType.InvalidSyntax)
        } as List <GraphQLError>
    }
}
