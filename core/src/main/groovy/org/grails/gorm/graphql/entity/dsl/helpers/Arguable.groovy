package org.grails.gorm.graphql.entity.dsl.helpers

import graphql.schema.GraphQLArgument
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.entity.arguments.ComplexArgument
import org.grails.gorm.graphql.entity.arguments.CustomArgument
import org.grails.gorm.graphql.entity.arguments.SimpleArgument
import org.grails.gorm.graphql.types.GraphQLTypeManager

/**
 * Decorates a class w/ support for GraphQLArguments
 *
 * @author James Kleeh
 * @since 2.0.2
 */
@CompileStatic
trait Arguable extends ExecutesClosures {

    List<CustomArgument> arguments = []

    /**
     * @param typeManager The returnType manager used to retrieve GraphQL types
     * @param mappingContext The GORM datastore mapping context used for retrieving information on entities
     * @return A collection of GraphQLArgument's, if any are defined
     */
    List<GraphQLArgument> getArguments(GraphQLTypeManager typeManager, MappingContext mappingContext) {
        arguments.collect {
            it.getArgument(typeManager, mappingContext).build()
        }
    }

}
