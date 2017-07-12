package org.grails.gorm.graphql.entity.property

/**
 * Represents what type of property is being created
 *
 * @author James Kleeh
 */
enum GraphQLPropertyType {
    //For returning data
    OUTPUT,

    //For creating data
    CREATE,

    //For updating data (typically the same as create except nulls allowed)
    UPDATE,

    //For supplying association data
    UPDATE_NESTED
}