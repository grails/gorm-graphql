package org.grails.gorm.graphql.types.input

import groovy.transform.CompileStatic

import static graphql.schema.GraphQLInputObjectField.newInputObjectField
import static graphql.schema.GraphQLInputObjectType.newInputObject
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.types.GraphQLTypeManager

@CompileStatic
abstract class InputObjectTypeBuilder {

    protected Map<PersistentEntity, GraphQLInputObjectType> objectTypeCache = [:]
    protected GraphQLDomainPropertyManager propertyManager
    protected GraphQLTypeManager typeManager

    InputObjectTypeBuilder(GraphQLDomainPropertyManager propertyManager, GraphQLTypeManager typeManager) {
        this.typeManager = typeManager
        this.propertyManager = propertyManager
    }

    abstract GraphQLDomainPropertyManager.Builder getBuilder()

    abstract GraphQLPropertyType getType()

    protected GraphQLInputObjectField.Builder buildInputField(GraphQLDomainProperty prop, GraphQLPropertyType type) {
        newInputObjectField()
                .name(prop.name)
                .description(prop.description)
                .type((GraphQLInputType)prop.getGraphQLType(typeManager, type))
    }

    GraphQLInputObjectType build(PersistentEntity entity) {

        GraphQLInputObjectType inputObjectType

        if (!objectTypeCache.containsKey(entity)) {
            final String DESCRIPTION = GraphQLEntityHelper.getDescription(entity)

            List<GraphQLDomainProperty> properties = builder.getProperties(entity)

            GraphQLInputObjectType.Builder inputObj = newInputObject()
                    .name(typeManager.namingConvention.getType(entity, type))
                    .description(DESCRIPTION)

            for (GraphQLDomainProperty prop: properties) {
                if (prop.input) {
                    inputObj.field(buildInputField(prop, type))
                }
            }

            inputObjectType = inputObj.build()
            objectTypeCache.put(entity, inputObjectType)
            inputObjectType
        }
        else {
            objectTypeCache.get(entity)
        }


    }
}
