package org.grails.gorm.graphql.entity.property

import groovy.transform.CompileStatic

/**
 * Represents what type of property is being created
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
enum GraphQLPropertyType {

    /**
     * For returning data
     */
    OUTPUT,

    /**
     * For creating data
     */
    CREATE,

    /**
     * For updating data (typically the same as create except nulls allowed)
     */
    UPDATE,

    /**
     * For supplying association data
     */
    INPUT_NESTED
}
