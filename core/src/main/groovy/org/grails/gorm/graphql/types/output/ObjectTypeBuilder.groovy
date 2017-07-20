package org.grails.gorm.graphql.types.output

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.fetcher.impl.ClosureDataFetcher
import org.grails.gorm.graphql.response.errors.GraphQLErrorsResponseHandler
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLObjectType.newObject

/**
 * A base class used to create object types that represent an entity
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
abstract class ObjectTypeBuilder {

    protected Map<PersistentEntity, GraphQLObjectType> objectTypeCache = [:]
    protected GraphQLDomainPropertyManager propertyManager
    protected GraphQLTypeManager typeManager
    protected GraphQLErrorsResponseHandler errorsResponseHandler

    ObjectTypeBuilder(GraphQLDomainPropertyManager propertyManager,
                      GraphQLTypeManager typeManager,
                      GraphQLErrorsResponseHandler errorsResponseHandler) {
        this.typeManager = typeManager
        this.propertyManager = propertyManager
        this.errorsResponseHandler = errorsResponseHandler
    }

    abstract GraphQLDomainPropertyManager.Builder getBuilder()

    abstract GraphQLPropertyType getType()

    protected GraphQLFieldDefinition.Builder buildField(GraphQLDomainProperty prop) {
        newFieldDefinition()
                .name(prop.name)
                .deprecate(prop.deprecationReason)
                .description(prop.description)
                .dataFetcher(prop.dataFetcher ? new ClosureDataFetcher(prop.dataFetcher) : null)
                .type((GraphQLOutputType)prop.getGraphQLType(typeManager, type))
    }

    GraphQLObjectType build(PersistentEntity entity) {

        GraphQLObjectType objectType

        if (objectTypeCache.containsKey(entity)) {
            objectTypeCache.get(entity)
        }
        else {
            final String DESCRIPTION = GraphQLEntityHelper.getDescription(entity)

            List<GraphQLDomainProperty> properties = builder.getProperties(entity)

            GraphQLObjectType.Builder obj = newObject()
                    .name(typeManager.namingConvention.getType(entity, type))
                    .description(DESCRIPTION)

            for (GraphQLDomainProperty prop: properties) {
                if (prop.output) {
                    obj.field(buildField(prop))
                }
            }

            if (errorsResponseHandler != null) {
                obj.field(errorsResponseHandler.fieldDefinition)
            }

            objectType = obj.build()
            objectTypeCache.put(entity, objectType)
            objectType
        }
    }

}
