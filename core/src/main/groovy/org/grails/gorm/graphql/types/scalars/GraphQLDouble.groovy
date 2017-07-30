package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.FloatingNumberCoercion

/**
 * Default {@link Double} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLDouble extends GraphQLScalarType {

    GraphQLDouble() {
        super('Double', 'Built-in Double', new FloatingNumberCoercion<Double>())
    }
}
