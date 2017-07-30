package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.TimestampCoercion

/**
 * Default {@link java.sql.Timestamp} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLTimestamp extends GraphQLScalarType {

    GraphQLTimestamp() {
        super('Timestamp', 'Accepts a numer or a string in the format "yyyy-[m]m-[d]d hh:mm:ss[.f...]"', new TimestampCoercion())
    }
}
