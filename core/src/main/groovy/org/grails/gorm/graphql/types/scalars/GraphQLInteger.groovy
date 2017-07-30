package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.WholeNumberCoercion

/**
 * Default {@link Integer} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLInteger extends GraphQLScalarType {

    GraphQLInteger() {
        super('Integer', 'Built-in Integer', new WholeNumberCoercion<Integer>())
    }
}
