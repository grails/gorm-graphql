package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.CharacterArrayCoercion

/**
 * Default {@link Character[]} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLCharacterArray extends GraphQLScalarType {

    GraphQLCharacterArray() {
        super('CharacterArray', 'Built-in Character Array', new CharacterArrayCoercion<Character[]>())
    }
}
