package org.grails.gorm.graphql.entity.property.impl

import org.grails.gorm.graphql.entity.property.GraphQLOperationType

import static graphql.schema.GraphQLList.list
import graphql.schema.GraphQLType
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.datastore.mapping.model.types.ToMany
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager
import java.lang.reflect.Field

/**
 * Implementation of {@link GraphQLDomainProperty} to represent a property
 * on a GORM entity
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class PersistentGraphQLProperty implements GraphQLDomainProperty {

    final String name
    final Class type
    final boolean collection
    final boolean nullable
    String description
    String deprecationReason
    final boolean input
    final boolean output
    final Closure dataFetcher

    PersistentProperty property
    private MappingContext mappingContext

    PersistentGraphQLProperty(MappingContext mappingContext, PersistentProperty property, GraphQLPropertyMapping mapping) {
        this.property = property
        this.mappingContext = mappingContext
        this.name = property.name
        this.type = getBaseType(property)
        this.collection = (property instanceof ToMany)
        if (mapping.nullable != null) {
            this.nullable = mapping.nullable
        }
        else {
            this.nullable = property.mapping.mappedForm.nullable
        }
        this.output = mapping.output
        this.input = mapping.input
        this.description = mapping.description
        this.deprecationReason = mapping.deprecationReason
        this.dataFetcher = mapping.dataFetcher
        final String DEFAULT_DEPRECATION_REASON = 'Deprecated'
        try {
            Field field = property.owner.javaClass.getDeclaredField(property.name)
            if (field != null) {
                GraphQL graphQL = field.getAnnotation(GraphQL)
                if (graphQL != null) {
                    if (description == null) {
                        description = graphQL.value()
                    }
                    if (deprecationReason == null && !graphQL.deprecationReason().empty) {
                        deprecationReason = graphQL.deprecationReason()
                    }
                    if (graphQL.deprecated() && deprecationReason == null) {
                        deprecationReason = DEFAULT_DEPRECATION_REASON
                    }
                }
                if (field.getAnnotation(Deprecated) != null && deprecationReason == null) {
                    deprecationReason = DEFAULT_DEPRECATION_REASON
                }
            }
        } catch (NoSuchFieldException e) { }

        if (mapping.deprecated && deprecationReason == null) {
            deprecationReason = DEFAULT_DEPRECATION_REASON
        }
    }

    protected Class getBaseType(PersistentProperty property) {
        if (property instanceof Association) {
            Association association = (Association)property
            if (association.basic) {
                ((Basic) property).componentType
            }
            else {
                association.associatedEntity.javaClass
            }
        }
        else {
            property.type
        }
    }

    @Override
    boolean isDeprecated() {
        deprecationReason != null
    }

    @Override
    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType) {
        GraphQLType graphQLType

        if (type.enum) {
            graphQLType = typeManager.getEnumType(type, nullable)
        }
        else {
            boolean embedded = false
            PersistentEntity entity
            if (property instanceof Association) {
                entity = ((Association)property).associatedEntity
                embedded = ((Association)property).embedded
            }
            if (entity == null) {
                entity = mappingContext.getPersistentEntity(type.name)
            }
            if (entity != null) {
                if (propertyType.operationType == GraphQLOperationType.OUTPUT) {
                    if (embedded) {
                        graphQLType = typeManager.getQueryType(entity, propertyType.embeddedType)
                    }
                    else if (GraphQLEntityHelper.getMapping(entity) != null) {
                        graphQLType = typeManager.createReference(entity, propertyType)
                    }
                    else {
                        graphQLType = typeManager.getQueryType(entity, propertyType.nestedType)
                    }
                }
                else {
                    GraphQLPropertyType mutationType
                    if (embedded) {
                        mutationType = propertyType.embeddedType
                    }
                    else {
                        mutationType = propertyType.nestedType
                    }
                    graphQLType = typeManager.getMutationType(entity, mutationType, nullable)
                }
            }
            else {
                graphQLType = typeManager.getType(type, nullable)
            }
        }

        if (collection) {
            graphQLType = list(graphQLType)
        }

        graphQLType
    }
}
