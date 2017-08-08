package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.ByteArrayCoercion

/**
 * Default {@link Byte[]} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLByteArray extends GraphQLScalarType {

    GraphQLByteArray() {
        super('ByteArray', 'Built-in Byte Array', new ByteArrayCoercion())
    }
}
