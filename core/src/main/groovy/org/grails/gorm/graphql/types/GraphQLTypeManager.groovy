package org.grails.gorm.graphql.types

import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLType
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType

/**
 * An interface for handling type conversion and creation with GraphQL
 *
 * @author James Kleeh
 */
interface GraphQLTypeManager {

    GraphQLType getType(Class clazz)

    GraphQLType getType(Class clazz, boolean nullable)

    void registerType(Class clazz, GraphQLType type)

    GraphQLType getEnumType(Class clazz, boolean nullable)

    GraphQLType createReference(PersistentEntity entity, GraphQLPropertyType type)

    GraphQLInputObjectType createUpdateType(PersistentEntity entity, GraphQLInputObjectType createType)
}
