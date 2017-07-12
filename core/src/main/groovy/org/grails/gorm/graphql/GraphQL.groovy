package org.grails.gorm.graphql

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.TYPE, ElementType.FIELD])
@Retention(RetentionPolicy.RUNTIME)
@interface GraphQL {

    String value()

    boolean deprecated() default false

    String deprecationReason() default ''
}