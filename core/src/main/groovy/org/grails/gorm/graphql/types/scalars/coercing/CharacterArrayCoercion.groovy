package org.grails.gorm.graphql.types.scalars.coercing

import graphql.Scalars
import graphql.language.ArrayValue
import graphql.language.Value
import graphql.schema.Coercing
import groovy.transform.CompileStatic
import java.lang.reflect.Array

/**
 * Conversion class for string arrays
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class CharacterArrayCoercion implements Coercing<Character[], Character[]> {

    Coercing<Character, Character> charCoercion = Scalars.GraphQLChar.coercing

    @Override
    Character[] serialize(Object input) {
        if (input instanceof Character[]) {
            (Character[]) input
        }
        else {
            null
        }
    }

    @Override
    Character[] parseValue(Object input) {
        if (input instanceof Collection) {
            Collection collection = (Collection) input
            Character[] characters = new Character[collection.size()]
            for (int i = 0; i < characters.length; i++) {
                characters[i] = charCoercion.serialize(((Collection)input)[i].toString())
            }
            characters
        }
        else if (input.class.array) {
            if (input instanceof Character[]) {
                (Character[]) input
            }
            else {
                Character[] strings = new Character[Array.getLength(input)]
                for (int i = 0; i < strings.length; i++) {
                    strings[i] = charCoercion.serialize(Array.get(input, i).toString())
                }
                strings
            }
        }
        else {
            null
        }
    }

    @Override
    Character[] parseLiteral(Object input) {
        if (input instanceof ArrayValue) {
            List<Character> returnList = []
            List<Value> values = ((ArrayValue) input).values
            for (Value value: values) {
                returnList.add(charCoercion.parseLiteral(value))
            }
            (Character[])returnList.toArray()
        }
        else {
            null
        }
    }
}
