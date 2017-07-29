package org.grails.gorm.graphql.entity.dsl.helpers

trait Defaultable<T> {

    Object defaultValue

    T defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue
        (T)this
    }
}