package org.grails.gorm.graphql.entity.operations

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

/**
 * A class to store data about an argument used in
 * a custom query or mutation.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@Builder(builderStrategy = SimpleStrategy, prefix = '')
class CustomArgument extends ReturnsType<CustomArgument> {

    String description
    Object defaultValue
    boolean nullable

}
