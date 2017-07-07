package org.grails.gorm.graphql.entity.property

import graphql.schema.GraphQLType
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.types.GraphQLTypeManager

import java.lang.reflect.Field

import static graphql.schema.GraphQLList.list

/**
 * Created by jameskleeh on 7/6/17.
 */
class PersistentGraphQLProperty implements GraphQLDomainProperty {

    final String name
    final boolean collection
    final boolean nullable
    String description
    String deprecationReason
    final boolean input
    final boolean output

    PersistentProperty property
    private MappingContext mappingContext

    PersistentGraphQLProperty(MappingContext mappingContext, PersistentProperty property, GraphQLPropertyMapping mapping) {
        this.property = property
        this.mappingContext = mappingContext
        this.name = property.name
        this.collection = (property instanceof Association)
        this.nullable = property.owner.isIdentityName(property.name) || property.mapping.mappedForm.nullable
        this.output = mapping.output
        this.input = mapping.input
        this.description = mapping.description
        this.deprecationReason = mapping.deprecationReason
        try {
            Field field = property.owner.javaClass.getField(property.name)
            if (field != null) {
                final String defaultDeprecationReason = 'Deprecated'
                GraphQL graphQL = field.getAnnotation(GraphQL)
                if (graphQL != null) {
                    if (description == null) {
                        description = graphQL.value()
                    }
                    if (deprecationReason == null) {
                        deprecationReason = graphQL.deprecationReason()
                    }
                    if (graphQL.deprecated() && deprecationReason == null) {
                        deprecationReason = defaultDeprecationReason
                    }
                }
                if (field.getAnnotation(Deprecated) != null && deprecationReason == null) {
                    deprecationReason = defaultDeprecationReason
                }
            }
        } catch (NoSuchFieldException e) {}
    }

    @Override
    boolean isDeprecated() {
        deprecationReason != null
    }

    @Override
    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType) {
        GraphQLType type

        if (property instanceof Association) {
            if (property.basic) {
                Class componentType = ((Basic) property).componentType
                if (mappingContext.mappingFactory.isSimpleType(componentType.name)) {
                    type = typeManager.getType(componentType)
                }
                else if (componentType.enum) {
                    type = typeManager.buildEnumType(componentType)
                }
                else {
                    throw new RuntimeException("Unsure of how to handle type definition of basic association ${property.toString()}. Not a simple type or enum.")
                }
            }
            else {
                type = typeManager.getReference(((Association)property).associatedEntity, propertyType)
            }
            type = list(type)
        }
        else {
            type = typeManager.getType(property.type, nullable)
        }

        type
    }
}
