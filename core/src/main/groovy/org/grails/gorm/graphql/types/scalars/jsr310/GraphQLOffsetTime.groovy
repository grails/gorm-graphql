package org.grails.gorm.graphql.types.scalars.jsr310

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * Default {@link java.time.OffsetTime} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLOffsetTime extends GraphQLScalarType {

    GraphQLOffsetTime(Coercing coercing) {
        super('OffsetTime', 'Built-in OffsetTime', coercing)
    }
}
