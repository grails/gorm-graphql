package org.grails.gorm.graphql.entity.dsl.helpers

import groovy.transform.CompileStatic

@CompileStatic
trait ExecutesClosures {

    static void withDelegate(Closure closure, Object delegate) {
        if (closure != null) {
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.delegate = delegate

            try {
                closure.call()
            } finally {
                closure.delegate = null
            }
        }
    }
}
