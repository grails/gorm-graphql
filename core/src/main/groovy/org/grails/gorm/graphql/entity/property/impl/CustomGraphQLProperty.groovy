package org.grails.gorm.graphql.entity.property.impl

import graphql.schema.GraphQLType
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager

/**
 * Implementation of {@link GraphQLDomainProperty} to be used to define
 * additional properties beyond the ones defined in GORM entities
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@AutoClone
@Builder(builderStrategy = SimpleStrategy, prefix = '')
@CompileStatic
class CustomGraphQLProperty implements GraphQLDomainProperty {

    String name
    Class type
    String description = null
    boolean deprecated = false
    String deprecationReason = null
    boolean input = true
    boolean output = true
    boolean nullable = true
    boolean collection = false
    Closure dataFetcher = null

    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType) {
        typeManager.getType(type, nullable)
    }

    static CustomGraphQLProperty newProperty() {
        new CustomGraphQLProperty()
    }
}
