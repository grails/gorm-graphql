package org.grails.gorm.graphql.errors

import graphql.Scalars
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormValidateable
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLNonNull.nonNull
import static graphql.schema.GraphQLObjectType.newObject


@CompileStatic
class DefaultGraphQLErrorsOutputHandler extends CachingGraphQLErrorsOutputHandler {

    private MessageSource messageSource

    DefaultGraphQLErrorsOutputHandler(MessageSource messageSource) {
        this.messageSource = messageSource
    }

    protected Locale getLocale(DataFetchingEnvironment environment) {
        if (environment.context instanceof Map) {
            Map context = (Map)environment.context
            if (context.containsKey('locale')) {
                Object localContext = context.get('locale')
                if (localContext instanceof Locale) {
                    return (Locale)localContext
                }
            }
        }
        Locale.default
    }

    protected DataFetcher fieldFetcher = new DataFetcher<String>() {
        @Override
        String get(DataFetchingEnvironment environment) {
            ((FieldError) environment.source).field
        }
    }

    protected DataFetcher messageFetcher = new DataFetcher<String>() {
        @Override
        String get(DataFetchingEnvironment environment) {
            messageSource.getMessage((FieldError) environment.source, getLocale(environment))
        }
    }

    protected DataFetcher errorsFetcher = new DataFetcher<List<FieldError>>() {
        @Override
        List<FieldError> get(DataFetchingEnvironment environment) {
            ((GormValidateable)environment.source).errors.fieldErrors
        }
    }



    private GraphQLObjectType _definition

    @Override
    protected GraphQLObjectType buildDefinition() {
        if (_definition != null) {
            return GraphQLObjectType.reference(_definition.name)
        }

        _definition = newObject()
            .name("Error")
            .field(newFieldDefinition()
                .name("field")
                .type(nonNull(Scalars.GraphQLString))
                .dataFetcher(fieldFetcher)
            )
            .field(newFieldDefinition()
                .name("message")
                .type(Scalars.GraphQLString)
                .dataFetcher(messageFetcher)
            ).build()

        _definition
    }

    @Override
    GraphQLFieldDefinition getFieldDefinition() {
        newFieldDefinition()
            .name("errors")
            .type(list(definition))
            .dataFetcher(errorsFetcher).build()
    }
}
