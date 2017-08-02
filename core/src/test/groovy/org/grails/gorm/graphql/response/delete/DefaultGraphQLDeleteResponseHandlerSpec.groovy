package org.grails.gorm.graphql.response.delete

import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import org.grails.gorm.graphql.testing.GraphQLSchemaSpec
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.grails.gorm.graphql.types.scalars.GraphQLBoolean
import spock.lang.Shared
import spock.lang.Specification

class DefaultGraphQLDeleteResponseHandlerSpec extends Specification implements GraphQLSchemaSpec {

    GraphQLDeleteResponseHandler handler

    @Shared GraphQLTypeManager typeManager

    void setupSpec() {
        typeManager = Stub(GraphQLTypeManager) {
            getType(Boolean, false) >> {
                GraphQLNonNull.nonNull(new GraphQLBoolean())
            }
        }
    }

    void setup() {
        handler = new DefaultGraphQLDeleteResponseHandler()
    }

    void "test the result is cached"() {
        expect:
        handler.getObjectType(typeManager) == handler.getObjectType(typeManager)
    }

    void "test the return data"() {
        expect:
        handler.createResponse(null, false) == [success: false]
        handler.createResponse(null, true) == [success: true]
    }

    void "test the object type definition"() {
        GraphQLObjectType type = handler.getObjectType(typeManager)

        expect:
        type.name == 'DeleteResult'
        type.description == 'Whether or not the operation was successful'
        type.interfaces.empty
        type.fieldDefinitions.size() == 1
        type.fieldDefinitions[0].name == 'success'
        unwrap(null, type.fieldDefinitions[0].type) instanceof GraphQLBoolean
    }
}
