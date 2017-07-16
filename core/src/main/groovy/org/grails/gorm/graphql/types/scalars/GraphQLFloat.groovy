package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.FloatCoercion

/**
 * Default {@link Float} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLFloat extends GraphQLScalarType {

    GraphQLFloat() {
        super('Float', 'Built-in Float', new FloatCoercion())
    }
}
