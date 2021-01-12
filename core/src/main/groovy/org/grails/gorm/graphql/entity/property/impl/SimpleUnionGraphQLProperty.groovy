package org.grails.gorm.graphql.entity.property.impl

import graphql.TypeResolutionEnvironment
import graphql.schema.*
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.entity.dsl.helpers.CustomTyped
import org.grails.gorm.graphql.entity.dsl.helpers.Typed
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLUnionType.newUnionType

/**
 * Used to represent a custom union property consisting of simple unioned types
 *
 * @author James Hardwick
 * @since 2.0.2
 */
@AutoClone
@CompileStatic
class SimpleUnionGraphQLProperty extends UnionGraphQLProperty<SimpleUnionGraphQLProperty> {

    String typeName
    private Set<Class> unionedTypes = []
    private Set<GraphQLObjectType> graphQLTypesMap = []


    SimpleUnionGraphQLProperty typeName(String typeName) {
        this.typeName = typeName
        this
    }

    SimpleUnionGraphQLProperty setUnionTypes(Set<Class> classes) {
        this.unionedTypes = classes
        this
    }

    @Override
    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType) {
        unionedTypes.each { type ->
            PersistentEntity entity = mappingContext?.getPersistentEntity(type.name)
            graphQLTypesMap.add((GraphQLObjectType)typeManager.getQueryType(entity, propertyType))
        }

        String name = typeManager.namingConvention.getType(typeName, propertyType)
        GraphQLUnionType.Builder obj = newUnionType()
                .name(name)
                .description(description)
                .typeResolver(new TypeResolver() {
                    @Override
                    GraphQLObjectType getType(TypeResolutionEnvironment env) {
                        String resolvedName = typeManager.namingConvention.getType(env.getObject().class.simpleName, propertyType)
                        (GraphQLObjectType)env.schema.getType(resolvedName)
                    }
                })

        for(GraphQLObjectType possibleType : graphQLTypesMap) {
            obj.possibleType(possibleType)
        }

        GraphQLOutputType type = obj.build()
        if (collection) {
            type = GraphQLList.list(type)
        }
        type
    }

    @Override
    Set<GraphQLObjectType> getUnionTypes() {
        graphQLTypesMap
    }

    void validate() {
        super.validate()

        if (typeName == null) {
            throw new IllegalArgumentException('The type name must be specified for custom properties with a complex type')
        }
    }
}
