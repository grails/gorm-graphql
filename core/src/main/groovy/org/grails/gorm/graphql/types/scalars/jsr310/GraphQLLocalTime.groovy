package org.grails.gorm.graphql.types.scalars.jsr310

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * Default {@link java.time.LocalTime} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLLocalTime extends GraphQLScalarType {

    GraphQLLocalTime(Coercing coercing) {
        super('LocalTime', 'Built-in LocalTime', coercing)
    }
}
