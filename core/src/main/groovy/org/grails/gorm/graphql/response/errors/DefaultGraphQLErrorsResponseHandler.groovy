package org.grails.gorm.graphql.response.errors

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormValidateable
import org.grails.gorm.graphql.response.GraphQLLocalResolver
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.springframework.context.MessageSource
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLObjectType.newObject

import org.springframework.validation.ObjectError

/**
 * The default way to respond with validation errors in GraphQL.
 * Will look for the locale in the environment context to properly format
 * error messages. Defaults to {@link Locale#getDefault()}.
 *
 * errors {
 *     field
 *     message
 * }
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DefaultGraphQLErrorsResponseHandler implements GraphQLErrorsResponseHandler {

    protected MessageSource messageSource
    protected String name = 'Errors'
    protected String description = 'Validation Errors'
    protected String fieldName = 'errors'
    protected String fieldDescription = 'Validation errors on the entity'

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
        else if(environment.context instanceof GraphQLLocalResolver){
            return ((GraphQLLocalResolver)environment.context).resolveLocale()
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
            messageSource.getMessage((ObjectError) environment.source, getLocale(environment))
        }
    }
    protected DataFetcher globalErrorsFetcher = new DataFetcher<List<String>>() {
        @Override
        List<String> get(DataFetchingEnvironment environment) {
            Iterator<ObjectError> globalObjectErrors = ((Errors)environment.source).globalErrors.iterator()
            List<String> objectErrors = []
            while(globalObjectErrors.hasNext()){
                objectErrors.add(messageSource.getMessage(globalObjectErrors.next(), getLocale(environment)))
            }
            return objectErrors
        }
    }

    protected DataFetcher fieldErrorsFetcher = new DataFetcher<List<FieldError>>() {
        @Override
        List<FieldError> get(DataFetchingEnvironment environment) {
            ((Errors)environment.source).fieldErrors
        }
    }
    

    protected DataFetcher errorsFetcher = new DataFetcher<Errors>() {
        @Override
        Errors get(DataFetchingEnvironment environment) {
            ((GormValidateable)environment.source).errors
        }
    }

    protected List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLTypeManager typeManager) {
        [newFieldDefinition()
             .name('globalErrors')
             .type(list((GraphQLOutputType)typeManager.getType(String, false)))
             .dataFetcher(globalErrorsFetcher)
             .build(),

         newFieldDefinition()
             .name('fieldErrors')
             .type(buildFieldErrorsDefinition(typeManager))
             .dataFetcher(fieldErrorsFetcher)
             .build()]        
        
    }
    
    protected List<GraphQLFieldDefinition> getFieldErrorDefinitions(GraphQLTypeManager typeManager) {
        [newFieldDefinition()
             .name('field')
             .type((GraphQLOutputType)typeManager.getType(String, false))
             .dataFetcher(fieldFetcher)
             .build(),
         newFieldDefinition()
             .name('message')
             .type((GraphQLOutputType)typeManager.getType(String))
             .dataFetcher(messageFetcher)
             .build()
        ]
    }
    
    protected GraphQLObjectType buildFieldErrorsDefinition(GraphQLTypeManager typeManager) {
        newObject()
            .name('FieldErrors')
            .description(description)
            .fields(getFieldErrorDefinitions(typeManager))
            .build()
    }
    
    protected GraphQLObjectType buildDefinition(GraphQLTypeManager typeManager) {
        newObject()
            .name(name)
            .description(description)
            .fields(getFieldDefinitions(typeManager))
            .build()
    }

    private GraphQLFieldDefinition cachedDefinition

    @Override
    GraphQLFieldDefinition getFieldDefinition(GraphQLTypeManager typeManager) {
        if (cachedDefinition == null) {
            cachedDefinition = newFieldDefinition()
                    .name(fieldName)
                    .description(fieldDescription)
                    .type(buildDefinition(typeManager))
                    .dataFetcher(errorsFetcher).build()
        }
        cachedDefinition
    }
}
