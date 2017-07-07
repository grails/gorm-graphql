package org.grails.gorm.graphql.response.errors

import graphql.Scalars
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormValidateable
import org.grails.gorm.graphql.response.CachingGraphQLResponseHandler
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLNonNull.nonNull
import static graphql.schema.GraphQLObjectType.newObject


@CompileStatic
class DefaultGraphQLErrorsResponseHandler extends CachingGraphQLResponseHandler implements GraphQLErrorsResponseHandler {

    protected MessageSource messageSource
    protected String name = "Error"
    protected String description = "Validation Errors"
    protected String fieldName = "errors"
    protected String fieldDescription = "A list of validation errors on the entity"

    DefaultGraphQLErrorsResponseHandler(MessageSource messageSource) {
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

    protected List<GraphQLFieldDefinition> getFieldDefinitions() {
        [newFieldDefinition()
            .name("field")
            .type(nonNull(Scalars.GraphQLString))
            .dataFetcher(fieldFetcher)
            .build(),

        newFieldDefinition()
            .name("message")
            .type(Scalars.GraphQLString)
            .dataFetcher(messageFetcher)
            .build()]
    }

    @Override
    protected GraphQLObjectType buildDefinition() {
        newObject()
            .name(name)
            .description(description)
            .fields(fieldDefinitions)
            .build()
    }

    @Override
    GraphQLFieldDefinition getFieldDefinition() {
        newFieldDefinition()
            .name(fieldName)
            .description(fieldDescription)
            .type(list(definition))
            .dataFetcher(errorsFetcher).build()
    }
}
