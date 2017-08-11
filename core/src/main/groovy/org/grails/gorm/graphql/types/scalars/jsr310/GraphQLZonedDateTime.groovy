package org.grails.gorm.graphql.types.scalars.jsr310

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * Default {@link java.time.ZonedDateTime} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLZonedDateTime extends GraphQLScalarType {

    GraphQLZonedDateTime(Coercing coercing) {
        super('ZonedDateTime', 'Built-in ZonedDateTime', coercing)
    }
}
