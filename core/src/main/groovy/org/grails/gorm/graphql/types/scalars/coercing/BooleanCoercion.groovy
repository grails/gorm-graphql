package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.BooleanValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Default {@link Boolean} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class BooleanCoercion implements Coercing<Boolean, Boolean> {

    @Override
    Boolean serialize(Object input) {
        if (input instanceof Boolean) {
            (Boolean) input
        }
        else {
            null
        }
    }

    @Override
    Boolean parseValue(Object input) {
        parseLiteral(input)
    }

    @Override
    Boolean parseLiteral(Object input) {
        if (input instanceof BooleanValue) {
            ((BooleanValue) input).value
        }
        else {
            null
        }
    }
}
