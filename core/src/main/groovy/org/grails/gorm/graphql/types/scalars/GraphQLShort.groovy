package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.WholeNumberCoercion

/**
 * Default {@link Short} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLShort extends GraphQLScalarType {

    GraphQLShort() {
        super('Short', 'Built-in Short', new WholeNumberCoercion<Short>())
    }
}
