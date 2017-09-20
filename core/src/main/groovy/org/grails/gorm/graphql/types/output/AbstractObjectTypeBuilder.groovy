package org.grails.gorm.graphql.types.output

import graphql.TypeResolutionEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import graphql.schema.TypeResolver
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.response.errors.GraphQLErrorsResponseHandler
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static GraphQLFieldDefinition.newFieldDefinition
import static GraphQLObjectType.newObject
import static GraphQLInterfaceType.newInterface

/**
 * A base class used to create object types that represent an entity
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
abstract class AbstractObjectTypeBuilder implements ObjectTypeBuilder {

    protected Map<PersistentEntity, GraphQLOutputType> objectTypeCache = [:]
    protected GraphQLDomainPropertyManager propertyManager
    protected GraphQLTypeManager typeManager
    protected GraphQLErrorsResponseHandler errorsResponseHandler

    AbstractObjectTypeBuilder(GraphQLDomainPropertyManager propertyManager,
                              GraphQLTypeManager typeManager,
                              GraphQLErrorsResponseHandler errorsResponseHandler) {
        this.typeManager = typeManager
        this.propertyManager = propertyManager
        this.errorsResponseHandler = errorsResponseHandler
    }

    abstract GraphQLDomainPropertyManager.Builder getBuilder()

    abstract GraphQLPropertyType getType()

    protected GraphQLFieldDefinition.Builder buildField(GraphQLDomainProperty prop) {
        GraphQLFieldDefinition.Builder field = newFieldDefinition()
                .name(prop.name)
                .deprecate(prop.deprecationReason)
                .description(prop.description)

        if (prop.dataFetcher != null) {
            field.dataFetcher(prop.dataFetcher)
        }

        field.type((GraphQLOutputType)prop.getGraphQLType(typeManager, type))
        field
    }

    GraphQLOutputType build(PersistentEntity entity) {

        GraphQLOutputType objectType

        if (objectTypeCache.containsKey(entity)) {
            objectTypeCache.get(entity)
        }
        else {
            final String DESCRIPTION = GraphQLEntityHelper.getDescription(entity)
            final String NAME = typeManager.namingConvention.getType(entity, type)

            List<GraphQLFieldDefinition> fields = new ArrayList<>(properties.size() + 1)

            List<GraphQLDomainProperty> properties = builder.getProperties(entity)
            for (GraphQLDomainProperty prop: properties) {
                if (prop.output) {
                    fields.add(buildField(prop).build())
                }
            }

            if (errorsResponseHandler != null) {
                fields.add(errorsResponseHandler.getFieldDefinition(typeManager))
            }

            boolean hasChildEntities = entity.root && !entity.mappingContext.getDirectChildEntities(entity).empty

            if (hasChildEntities && !type.embedded) {
                objectType = buildInterfaceType(entity, NAME, DESCRIPTION, fields)
            }
            else {
                objectType = buildObjectType(entity, NAME, DESCRIPTION, fields)
            }

            objectTypeCache.put(entity, objectType)
            objectType
        }
    }

    GraphQLObjectType buildObjectType(final PersistentEntity entity, final String name, final String description, final List<GraphQLFieldDefinition> fields) {

        GraphQLObjectType.Builder obj = newObject()
                .name(name)
                .description(description)
                .fields(fields)

        if (!entity.isRoot()) {
            obj.withInterface(typeManager.createReference(entity.rootEntity, GraphQLPropertyType.OUTPUT))
        }

        obj.build()
    }

    GraphQLInterfaceType buildInterfaceType(final PersistentEntity entity, final String name, final String description, final List<GraphQLFieldDefinition> fields) {

        GraphQLInterfaceType.Builder obj = newInterface()
                .name(name)
                .description(description)
                .fields(fields)
                .typeResolver(new TypeResolver() {
                    @Override
                    GraphQLObjectType getType(TypeResolutionEnvironment env) {
                        final String TYPE_NAME = typeManager.namingConvention.getType(env.object.class.simpleName, GraphQLPropertyType.OUTPUT)
                        (GraphQLObjectType)env.schema.getType(TYPE_NAME)
                    }
                })

        obj.build()
    }

}
