package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.TimeCoercion

/**
 * Default {@link java.sql.Time} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLTime extends GraphQLScalarType {

    GraphQLTime() {
        super('Time', 'Accepts a number or string in the format "hh:mm:ss"', new TimeCoercion())
    }
}
