package org.grails.gorm.graphql.entity.property

import graphql.schema.GraphQLType
import org.grails.gorm.graphql.types.GraphQLTypeManager

/**
 * Created by jameskleeh on 7/6/17.
 */
interface GraphQLDomainProperty {
    String getName()
    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType)
    String getDescription()
    boolean isDeprecated()
    String getDeprecationReason()
    boolean isInput()
    boolean isOutput()
    boolean isNullable()
    boolean isCollection()
}