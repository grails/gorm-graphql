package org.grails.gorm.graphql.entity.operations

import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLList
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLInputObjectField.newInputObjectField
/**
 * A class to store data about an argument used in
 * a custom query or mutation.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@Builder(builderStrategy = SimpleStrategy, prefix = '')
class CustomArgument extends ReturnsType<CustomArgument> {

    String description
    Object defaultValue
    boolean nullable = true

    GraphQLInputObjectType buildCustomType(GraphQLTypeManager typeManager) {
        GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
                .name(name.capitalize() + 'Custom')

        for (Map.Entry<String, Class> entry: customReturnFields) {
            builder.field(newInputObjectField()
                    .name(entry.key)
                    .type((GraphQLInputType)typeManager.getType(entry.value)))
        }
        builder.build()
    }

    GraphQLArgument.Builder getArgument(GraphQLTypeManager typeManager) {

        GraphQLInputType type
        if (customReturnFields != null) {
            type = buildCustomType(typeManager)
        }
        else {
            type = (GraphQLInputType)typeManager.getType(returnType, nullable)
        }

        if (collection) {
            type = GraphQLList.list(type)
        }

        newArgument()
            .name(name)
            .description(description)
            .defaultValue(defaultValue)
            .type(type)
    }
}
