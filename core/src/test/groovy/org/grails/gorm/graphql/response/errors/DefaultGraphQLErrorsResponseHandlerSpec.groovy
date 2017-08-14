package org.grails.gorm.graphql.response.errors

import graphql.Scalars
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import org.grails.datastore.gorm.GormValidateable
import org.grails.gorm.graphql.testing.GraphQLSchemaSpec
import org.grails.gorm.graphql.testing.MockDataFetchingEnvironment
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.springframework.context.MessageSource
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import spock.lang.Shared
import spock.lang.Specification

class DefaultGraphQLErrorsResponseHandlerSpec extends Specification implements GraphQLSchemaSpec {

    GraphQLErrorsResponseHandler handler
    MessageSource messageSource

    @Shared GraphQLTypeManager typeManager

    void setupSpec() {
        typeManager = Stub(GraphQLTypeManager) {
            getType(String, false) >> GraphQLNonNull.nonNull(Scalars.GraphQLString)
            getType(String) >> Scalars.GraphQLString
        }
    }

    void setup() {
        messageSource = Mock(MessageSource)
        handler = new DefaultGraphQLErrorsResponseHandler(messageSource)
    }

    void "test field definition is cached"() {
        expect:
        handler.getFieldDefinition(typeManager) == handler.getFieldDefinition(typeManager)
    }

    void "test field definition"() {
        given:
        FieldError fieldError = Mock(FieldError) {
            1 * getField() >> 'book'
        }
        1 * messageSource.getMessage(fieldError, Locale.default) >> 'hello'
        GormValidateable validateable = new MockValidateable()
        validateable.errors.rejectValue('foo', 'foo.not.valid')
        DataFetchingEnvironment mockFieldEnv = new MockDataFetchingEnvironment(source: fieldError)
        DataFetchingEnvironment mockObjectEnv = new MockDataFetchingEnvironment(source: validateable)


        when:
        GraphQLFieldDefinition field = handler.getFieldDefinition(typeManager)
        GraphQLObjectType type = field.type

        then:
        field.description == 'Validation errors on the entity'
        field.name == 'errors'
        (field.dataFetcher.get(mockObjectEnv)) instanceof Errors

        type.name == 'Errors'
        type.description == 'Validation Errors'
        type.fieldDefinitions.size() == 2
        type.fieldDefinitions[0].name == 'globalErrors'
        unwrap([null], type.fieldDefinitions[0].type) == Scalars.GraphQLString

        type.fieldDefinitions[1].name == 'fieldErrors'
        type.fieldDefinitions[1].type instanceof GraphQLObjectType
        
        when:
        type = (GraphQLObjectType) type.fieldDefinitions[1].type
        
        then:
        type.fieldDefinitions[0].name == 'field'
        unwrap(null, type.fieldDefinitions[0].type) == Scalars.GraphQLString
        type.fieldDefinitions[0].dataFetcher.get(mockFieldEnv) == 'book'


        type.fieldDefinitions[1].name == 'message'
        type.fieldDefinitions[1].type == Scalars.GraphQLString
        type.fieldDefinitions[1].dataFetcher.get(mockFieldEnv) == 'hello'
    }

    class MockValidateable implements GormValidateable {
        String foo
    }
}
