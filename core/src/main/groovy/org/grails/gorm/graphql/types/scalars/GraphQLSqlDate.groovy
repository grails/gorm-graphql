package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.SqlDateCoercion

/**
 * Default {@link java.sql.Date} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLSqlDate extends GraphQLScalarType {

    GraphQLSqlDate() {
        super('SqlDate', 'Accepts a number or a string in the format "yyyy-[m]m-[d]d"', new SqlDateCoercion())
    }
}
