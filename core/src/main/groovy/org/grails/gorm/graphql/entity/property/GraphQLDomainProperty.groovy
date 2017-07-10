package org.grails.gorm.graphql.entity.property

import graphql.schema.GraphQLType
import org.grails.gorm.graphql.types.GraphQLTypeManager

/**
 * An interface to describe a property to be used in the
 * creation of a GraphQL schema
 *
 * @author James Kleeh
 */
interface GraphQLDomainProperty {
    String getName()
    Class getType()
    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType)
    String getDescription()
    boolean isDeprecated()
    String getDeprecationReason()
    boolean isInput()
    boolean isOutput()
    boolean isNullable()
    boolean isCollection()
    Closure getDataFetcher()
}