package org.grails.gorm.graphql.types.scalars.coercing

import graphql.schema.Coercing
import groovy.transform.CompileStatic

@CompileStatic
class UUIDCoercion implements Coercing<UUID, UUID> {

    @Override
    UUID serialize(Object input) {
        if (input instanceof UUID) {
            return (UUID) input
        }
        else {
            return null
        }
    }

    @Override
    UUID parseValue(Object input) {
        return serialize(input)
    }

    @Override
    UUID parseLiteral(Object input) {
        if (input instanceof String) {
            UUID.fromString((String)input)
        }
        else {
            null
        }
    }
}
