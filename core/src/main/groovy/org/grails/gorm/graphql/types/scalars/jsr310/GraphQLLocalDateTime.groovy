package org.grails.gorm.graphql.types.scalars.jsr310

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * Default {@link java.time.LocalDateTime} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLLocalDateTime extends GraphQLScalarType {

    GraphQLLocalDateTime(Coercing coercing) {
        super('LocalDateTime', 'Built-in LocalDateTime', coercing)
    }
}
