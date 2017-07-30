package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.WholeNumberCoercion

/**
 * Default {@link Long} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLLong extends GraphQLScalarType {

    GraphQLLong() {
        super('Long', 'Built-in Long', new WholeNumberCoercion<Long>())
    }
}
