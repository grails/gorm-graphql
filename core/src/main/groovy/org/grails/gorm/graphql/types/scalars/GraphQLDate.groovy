package org.grails.gorm.graphql.types.scalars

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * Default {@link Date} scalar returnType
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLDate extends GraphQLScalarType {

    GraphQLDate(Coercing coercing) {
        super('Date', 'Built-in Date', coercing)
    }
}
