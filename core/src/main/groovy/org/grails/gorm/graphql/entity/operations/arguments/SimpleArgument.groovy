package org.grails.gorm.graphql.entity.operations.arguments

import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLList
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.entity.dsl.helpers.Defaultable
import org.grails.gorm.graphql.entity.dsl.helpers.Describable
import org.grails.gorm.graphql.entity.dsl.helpers.Named
import org.grails.gorm.graphql.entity.dsl.helpers.Nullable
import org.grails.gorm.graphql.entity.dsl.helpers.Typed
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLArgument.newArgument

@CompileStatic
class SimpleArgument extends CustomArgument<SimpleArgument> implements Typed<SimpleArgument> {

    @Override
    GraphQLInputType getType(GraphQLTypeManager typeManager, MappingContext mappingContext) {
        resolveInputType(typeManager, mappingContext, nullable)
    }

    void validate() {
        super.validate()

        if (returnType == null) {
            throw new IllegalArgumentException('A return type is required for creating arguments to custom operations')
        }
    }
}
