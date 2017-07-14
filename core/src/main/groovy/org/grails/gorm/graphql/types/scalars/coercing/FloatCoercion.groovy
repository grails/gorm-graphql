package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

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
        return serialize(input)
    }

    @Override
    Float parseLiteral(Object input) {
        if (input instanceof IntValue) {
            return ((IntValue) input).getValue().floatValue()
        }
        else if (input instanceof FloatValue) {
            return ((FloatValue) input).getValue().floatValue()
        }
        else {
            return null
        }
    }

}
