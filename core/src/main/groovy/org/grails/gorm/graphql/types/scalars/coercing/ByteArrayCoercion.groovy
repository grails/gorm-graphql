package org.grails.gorm.graphql.types.scalars.coercing

import graphql.Scalars
import graphql.language.ArrayValue
import graphql.language.Value
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Coercion class for whole number arrays
 *
 * @param <T> The type of the property
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class ByteArrayCoercion implements Coercing<Byte[], Byte[]> {

    Coercing<Byte, Byte> byteCoercion = Scalars.GraphQLByte.coercing

    @Override
    Byte[] serialize(Object input) {
        if (input instanceof Byte[]) {
            (Byte[]) input
        }
        else {
            null
        }
    }

    @Override
    Byte[] parseValue(Object input) {
        (Byte[])serialize(input)
    }

    @Override
    Byte[] parseLiteral(Object input) {
        if (input instanceof ArrayValue) {
            List<Byte> returnList = []
            List<Value> values = ((ArrayValue) input).values
            for (Value value: values) {
                returnList.add(byteCoercion.parseLiteral(value))
            }
            (Byte[])returnList.toArray()
        }
        else {
            null
        }
    }
}
