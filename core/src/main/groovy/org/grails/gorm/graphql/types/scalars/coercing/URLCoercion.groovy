package org.grails.gorm.graphql.types.scalars.coercing

import graphql.schema.Coercing
import groovy.transform.CompileStatic

@CompileStatic
class URLCoercion implements Coercing<URL, URL> {

    @Override
    URL serialize(Object input) {
        if (input instanceof URL) {
            (URL) input
        }
        else {
            null
        }
    }

    @Override
    URL parseValue(Object input) {
        serialize(input)
    }

    @Override
    URL parseLiteral(Object input) {
        if (input instanceof String) {
            new URL((String)input)
        }
        else {
            null
        }
    }
}
