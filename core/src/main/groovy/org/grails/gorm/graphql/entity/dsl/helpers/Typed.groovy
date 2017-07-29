package org.grails.gorm.graphql.entity.dsl.helpers

import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLType
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.types.GraphQLOperationType
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.grails.gorm.graphql.types.TypeNotFoundException

import static graphql.schema.GraphQLList.list

/**
 * Parses types for custom arguments, operations, and properties
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
trait Typed<T> {

    Class returnType
    boolean collection = false

    T returns(List<Class> list) {
        if (list.empty || list.size() > 1 || !(list[0] instanceof Class)) {
            throw new IllegalArgumentException('When setting the returnType of a custom operation or argument with a list, the list may only have one element that is a class.')
        }
        returnType = (Class)list[0]
        collection = true
        (T)this
    }

    T returns(Class clazz) {
        returnType = clazz
        collection = false
        (T)this
    }

    GraphQLInputType resolveInputType(GraphQLTypeManager typeManager, MappingContext mappingContext, boolean nullable) {
        (GraphQLInputType)resolveType(typeManager, mappingContext, GraphQLPropertyType.CREATE, nullable)
    }

    GraphQLType resolveType(GraphQLTypeManager typeManager, MappingContext mappingContext, GraphQLPropertyType propertyType, boolean nullable) {
        GraphQLType graphQLType
        Class type = returnType

        if (type.enum) {
            graphQLType = typeManager.getEnumType(type, nullable)
        }
        else if (typeManager.hasType(type)) {
            graphQLType = (GraphQLInputType)typeManager.getType(type, nullable)
        }
        else {
            PersistentEntity entity = mappingContext?.getPersistentEntity(type.name)

            if (entity != null) {
                if (propertyType.operationType == GraphQLOperationType.OUTPUT) {
                    propertyType = GraphQLPropertyType.OUTPUT
                    GraphQLMapping mapping = GraphQLEntityHelper.getMapping(entity)
                    if (mapping != null) {
                        graphQLType = typeManager.createReference(entity, propertyType)
                    }
                    else {
                        graphQLType = typeManager.getQueryType(entity, propertyType.nestedType)
                    }
                }
                else {
                    GraphQLPropertyType mutationType = propertyType.nestedType
                    graphQLType = typeManager.getMutationType(entity, mutationType, nullable)
                }
            }
            else {
                throw new TypeNotFoundException(type)
            }
        }

        if (collection) {
            graphQLType = list(graphQLType)
        }

        graphQLType
    }

    GraphQLOutputType resolveOutputType(GraphQLTypeManager typeManager, MappingContext mappingContext) {
        (GraphQLOutputType)resolveType(typeManager, mappingContext, GraphQLPropertyType.OUTPUT, true)
    }

}
