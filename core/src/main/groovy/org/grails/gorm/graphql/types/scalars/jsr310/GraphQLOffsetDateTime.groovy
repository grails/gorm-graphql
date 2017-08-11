package org.grails.gorm.graphql.types.scalars.jsr310

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * Default {@link java.time.OffsetDateTime} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLOffsetDateTime extends GraphQLScalarType {

    GraphQLOffsetDateTime(Coercing coercing) {
        super('OffsetDateTime', 'Built-in OffsetDateTime', coercing)
    }
}
