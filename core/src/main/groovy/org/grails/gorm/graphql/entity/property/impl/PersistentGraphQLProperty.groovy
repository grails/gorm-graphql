package org.grails.gorm.graphql.entity.property.impl

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
        this.nullable = !isIdentityName(property.owner, property.name) && property.mapping.mappedForm.nullable
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

    protected boolean isIdentityName(PersistentEntity entity, final String name) {
        if (entity.identity != null) {
            entity.identity.name == name
        }
        else if (entity.compositeIdentity != null) {
            entity.compositeIdentity.any { PersistentProperty prop -> prop.name == name }
        }
        else {
            false
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
        //It is expected at ths point that embedded properties have been "unwrapped"
        GraphQLType graphQLType

        boolean nullable = true
        if (propertyType == GraphQLPropertyType.CREATE) {
            nullable = this.nullable
        }

        if (type.enum) {
            graphQLType = typeManager.getEnumType(type, nullable)
        }
        else {
            PersistentEntity entity = mappingContext.getPersistentEntity(type.name)
            if (entity != null) {
                if (propertyType == GraphQLPropertyType.UPDATE || propertyType == GraphQLPropertyType.CREATE) {
                    graphQLType = typeManager.getMutationType(entity, GraphQLPropertyType.INPUT_NESTED)
                }
                else if (GraphQLEntityHelper.getMapping(entity) != null) {
                    graphQLType = typeManager.createReference(entity, propertyType)
                }
                else {
                    graphQLType = typeManager.getQueryType(entity)
                }
            } else {
                graphQLType = typeManager.getType(type, nullable)
            }
        }

        if (collection) {
            graphQLType = list(graphQLType)
        }

        graphQLType
    }
}
