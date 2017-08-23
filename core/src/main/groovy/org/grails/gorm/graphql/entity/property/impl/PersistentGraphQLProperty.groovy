package org.grails.gorm.graphql.entity.property.impl

import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.PersistentEntityValidator
import graphql.schema.DataFetcher
import graphql.schema.GraphQLType
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.datastore.mapping.model.types.ToMany
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.Schema
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.fetcher.impl.ClosureDataFetcher
import org.grails.gorm.graphql.types.GraphQLOperationType
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.springframework.validation.Validator

import java.lang.reflect.Field

import static graphql.schema.GraphQLList.list

/**
 * Implementation of {@link GraphQLDomainProperty} to represent a property
 * on a GORM entity
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class PersistentGraphQLProperty implements GraphQLDomainProperty {
    final boolean identity
    final int order
    final String name
    final Class type
    final boolean collection
    final boolean nullable
    String description
    String deprecationReason
    final boolean input
    final boolean output
    final DataFetcher dataFetcher

    PersistentProperty property
    private MappingContext mappingContext

    PersistentGraphQLProperty(MappingContext mappingContext, PersistentProperty property, GraphQLPropertyMapping mapping) {
        this.identity = isPartOfIdentity(property)
        this.order = getConstrainedOrder(property,mappingContext)
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
        this.dataFetcher = mapping.dataFetcher ? new ClosureDataFetcher(mapping.dataFetcher) : null
        try {
            Field field = property.owner.javaClass.getDeclaredField(property.name)
            if (field != null) {
                GraphQL graphQL = field.getAnnotation(GraphQL)
                if (graphQL != null) {
                    if (description == null && !graphQL.value().empty) {
                        description = graphQL.value()
                    }
                    if (deprecationReason == null && !graphQL.deprecationReason().empty) {
                        deprecationReason = graphQL.deprecationReason()
                    }
                    if (graphQL.deprecated() && deprecationReason == null) {
                        deprecationReason = Schema.DEFAULT_DEPRECATION_REASON
                    }
                }
                if (field.getAnnotation(Deprecated) != null && deprecationReason == null) {
                    deprecationReason = Schema.DEFAULT_DEPRECATION_REASON
                }
            }
        } catch (NoSuchFieldException e) { }

        if (mapping.deprecated && deprecationReason == null) {
            deprecationReason = Schema.DEFAULT_DEPRECATION_REASON
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
                Association association = ((Association)property)
                entity = association.associatedEntity
                embedded = association.embedded
            }
            if (entity == null) {
                entity = mappingContext.getPersistentEntity(type.name)
            }
            if (entity != null) {
                if (propertyType.operationType == GraphQLOperationType.OUTPUT) {
                    if (embedded) {
                        graphQLType = typeManager.getQueryType(entity, propertyType.embeddedType)
                    }
                    else {
                        graphQLType = typeManager.getQueryType(entity,  GraphQLPropertyType.OUTPUT)
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

    @Override
    int compareTo(GraphQLDomainProperty o) {
        int result = 0
        if(o instanceof PersistentGraphQLProperty){
            PersistentGraphQLProperty other = (PersistentGraphQLProperty) o
            
            result = (identity <=> other.identity) * -1 
            
            if(result == 0){
                result = order <=> other.order
            }
        }
        else if(identity){
            result = -1
        }
        else if(o instanceof CustomGraphQLProperty){
            result = order <=> ((CustomGraphQLProperty)o).order
        }
        return result?: name <=> o.name        
    }
    
    private static boolean isPartOfIdentity(PersistentProperty property){
        property.owner.identity?.name == property.name ||
            property.owner.compositeIdentity?.find{it.name == property.name}
    }
    private static int getConstrainedOrder(PersistentProperty property, MappingContext context){
        ConstrainedProperty constrainedProperty = getConstrainedProperty(property,context)
        constrainedProperty == null? Integer.MAX_VALUE : constrainedProperty.order
    }
    private static ConstrainedProperty getConstrainedProperty(PersistentProperty property, MappingContext context){
        Validator validator = context?.getEntityValidator(property.owner)
        if(validator instanceof PersistentEntityValidator){
            ((PersistentEntityValidator)validator).constrainedProperties?.get(property.name)
        }
    }
}
