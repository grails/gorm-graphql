package org.grails.gorm.graphql.entity.operations

import groovy.transform.CompileStatic

/**
 * Parses return types for custom arguments and operations
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class ReturnsType<T> {

    String name
    Class returnType
    boolean collection
    Map<String, Class> customReturnFields

    T  name(String name) {
        this.name = name
        (T)this
    }

    T type(List<Class> list) {
        if (list.size() > 1) {
            throw new IllegalArgumentException("When setting the type of a custom operation with a list, the list may only have one element.")
        }
        if (list[0] instanceof Map) {
            type(list[0])
        }
        else {
            returnType = list[0]
        }
        collection = true
        (T)this
    }

    T type(Map<String, Class> fields) {
        customReturnFields = fields
        (T)this
    }

    T type(Class clazz) {
        returnType = clazz
        collection = false
        (T)this
    }
}
