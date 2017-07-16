package org.grails.gorm.graphql.types.scalars.coercing

import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Default {@link UUID} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class UUIDCoercion implements Coercing<UUID, UUID> {

    @Override
    UUID serialize(Object input) {
        if (input instanceof UUID) {
            (UUID) input
        }
        else {
            null
        }
    }

    @Override
    UUID parseValue(Object input) {
        serialize(input)
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
