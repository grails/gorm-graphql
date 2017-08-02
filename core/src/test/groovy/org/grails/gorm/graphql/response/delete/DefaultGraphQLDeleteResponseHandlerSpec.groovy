package org.grails.gorm.graphql.response.delete

import graphql.schema.GraphQLObjectType
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.testing.GraphQLSchemaSpec
import org.grails.gorm.graphql.types.DefaultGraphQLTypeManager
import org.grails.gorm.graphql.types.scalars.GraphQLBoolean
import spock.lang.Specification

class DefaultGraphQLDeleteResponseHandlerSpec extends Specification implements GraphQLSchemaSpec {

    GraphQLDeleteResponseHandler handler

    void setup() {
        handler = new DefaultGraphQLDeleteResponseHandler(
                new DefaultGraphQLTypeManager(new GraphQLEntityNamingConvention(), null, new DefaultGraphQLDomainPropertyManager())
        )
    }

    void "test the result is cached"() {
        expect:
        handler.objectType == handler.objectType
    }

    void "test the return data"() {
        expect:
        handler.createResponse(null, false) == [success: false]
        handler.createResponse(null, true) == [success: true]
    }

    void "test the object type definition"() {
        GraphQLObjectType type = handler.objectType

        expect:
        type.name == 'DeleteResult'
        type.description == 'Whether or not the operation was successful'
        type.interfaces.empty
        type.fieldDefinitions.size() == 1
        type.fieldDefinitions[0].name == 'success'
        unwrap(null, type.fieldDefinitions[0].type) instanceof GraphQLBoolean
    }
}
