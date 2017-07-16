package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Default {@link Float} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class FloatCoercion implements Coercing<Float, Float> {

    @Override
    Float serialize(Object input) {
        if (input instanceof Float) {
            (Float) input
        }
        else {
            null
        }
    }

    @Override
    Float parseValue(Object input) {
        serialize(input)
    }

    @Override
    Float parseLiteral(Object input) {
        if (input instanceof IntValue) {
            ((IntValue) input).value.floatValue()
        }
        else if (input instanceof FloatValue) {
            ((FloatValue) input).value.floatValue()
        }
        else {
            null
        }
    }

}
