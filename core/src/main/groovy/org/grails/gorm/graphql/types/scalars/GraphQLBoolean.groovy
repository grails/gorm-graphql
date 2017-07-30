package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.BooleanCoercion

/**
 * Default {@link Boolean} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLBoolean extends GraphQLScalarType {

    GraphQLBoolean() {
        super('Boolean', 'Built-in Boolean', new BooleanCoercion())
    }
}
