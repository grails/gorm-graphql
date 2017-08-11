package org.grails.gorm.graphql.types.scalars.jsr310

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * Default {@link java.time.Instant} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLInstant extends GraphQLScalarType {

    GraphQLInstant(Coercing coercing) {
        super('Instant', 'Built-in Instant', coercing)
    }
}
