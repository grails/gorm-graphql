package org.grails.gorm.graphql.entity.property.impl

import graphql.schema.GraphQLType
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.entity.dsl.helpers.Typed
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager

@AutoClone
@CompileStatic
class SimpleGraphQLProperty extends CustomGraphQLProperty<SimpleGraphQLProperty> implements Typed<SimpleGraphQLProperty> {

    @Override
    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType) {
        resolveType(typeManager, mappingContext, propertyType, nullable)
    }
}
