package org.grails.gorm.graphql.types

/**
 * @author James Kleeh
 * @since 1.0.0
 */
class TypeNotFoundException extends RuntimeException {

    TypeNotFoundException(Class clazz) {
        super("A GraphQL returnType could not be found for ${clazz.name}")
    }
}
