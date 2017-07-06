package org.grails.gorm.graphql.types

import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLType
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType

interface GraphQLTypeManager {

    GraphQLType getType(Class clazz)

    GraphQLType getType(Class clazz, boolean nullable)

    void registerType(Class clazz, GraphQLType type)

    GraphQLEnumType buildEnumType(Class clazz)

    GraphQLType getReference(PersistentEntity entity, GraphQLPropertyType type)
}
