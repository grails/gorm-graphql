package org.grails.gorm.graphql.types.scalars.jsr310

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * Default {@link java.time.LocalDate} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLLocalDate extends GraphQLScalarType {

    GraphQLLocalDate(Coercing coercing) {
        super('LocalDate', 'Built-in LocalDate', coercing)
    }
}
