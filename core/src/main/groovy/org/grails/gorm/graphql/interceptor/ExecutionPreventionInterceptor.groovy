package org.grails.gorm.graphql.interceptor

/**
 * Created by jameskleeh on 7/20/17.
 */
interface ExecutionPreventionInterceptor {

    boolean shouldAllow()
}