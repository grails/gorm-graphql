package org.grails.gorm.graphql.entity.property

import graphql.schema.GraphQLType
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.gorm.graphql.GraphQL
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

    PersistentGraphQLProperty(MappingContext mappingContext, PersistentProperty property, boolean input, boolean output) {
        this.property = property
        this.mappingContext = mappingContext
        this.name = property.name
        this.collection = (property instanceof Association)
        this.nullable = property.owner.isIdentityName(property.name) || property.mapping.mappedForm.nullable
        this.output = output
        this.input = input
        try {
            Field field = property.owner.javaClass.getField(property.name)
            if (field != null) {
                GraphQL graphQL = field.getAnnotation(GraphQL)
                if (graphQL != null) {
                    description = graphQL.value()
                }
                if (graphQL != null && graphQL.deprecationReason() != null) {
                    deprecationReason = graphQL.deprecationReason()
                } else if ((graphQL != null && graphQL.deprecated()) || field.getAnnotation(Deprecated) != null) {
                    deprecationReason = "Deprecated"
                }
            }
        } catch (NoSuchFieldException e) {}
    }

    boolean isDeprecated() {
        deprecationReason != null
    }

    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType) {
        GraphQLType type

        if (property instanceof Association) {
            if (property.basic) {
                Class componentType = ((Basic) property).componentType
                if (mappingContext.mappingFactory.isSimpleType(componentType.name)) {
                    type = typeManager.getType(componentType)
                } else if (componentType.enum) {
                    type = typeManager.buildEnumType(componentType)
                } else {
                    throw new RuntimeException("Unsure of how to handle type definition of basic association ${property.toString()}. Not a simple type or enum.")
                }
            } else {
                type = typeManager.getReference(((Association)property).associatedEntity, propertyType)
            }
            type = list(type)
        } else {
            type = typeManager.getType(property.type, nullable)
        }

        type
    }
}
