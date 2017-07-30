package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.StringCoercion

/**
 * Default {@link Character} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLCharacter extends GraphQLScalarType {

    GraphQLCharacter() {
        super('Character', 'Built-in Character', new StringCoercion<Character>())
    }
}
