package org.grails.gorm.graphql.entity.property

import graphql.schema.GraphQLType
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.gorm.graphql.types.GraphQLTypeManager

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class AdditionalGraphQLProperty implements GraphQLDomainProperty {

    String name
    Class type
    String description = null
    boolean deprecated = false
    String deprecationReason = null
    boolean input = true
    boolean output = true
    boolean nullable = true
    boolean collection = false

    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType) {
        typeManager.getType(type, nullable)
    }

    static AdditionalGraphQLProperty newProperty() {
        new AdditionalGraphQLProperty()
    }
}
