package org.grails.gorm.graphql.types

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
    OUTPUT(GraphQLOperationType.OUTPUT, false, false),

    /**
     * For creating data
     */
    CREATE(GraphQLOperationType.CREATE, false, false),

    /**
     * For updating data (typically the same as create except nulls allowed)
     */
    UPDATE(GraphQLOperationType.UPDATE, false, false),

    /**
     * For supplying association data during a create
     */
    CREATE_NESTED(GraphQLOperationType.CREATE, false, true),

    /**
     * For supplying association data during an update
     */
    UPDATE_NESTED(GraphQLOperationType.UPDATE, false, true),

    /**
     * For creating embedded properties
     */
    CREATE_EMBEDDED(GraphQLOperationType.CREATE, true, false),

    /**
     * For updating embedded properties
     */
    UPDATE_EMBEDDED(GraphQLOperationType.UPDATE, true, false),

    /**
     * For displaying embedded properties
     */
    OUTPUT_EMBEDDED(GraphQLOperationType.OUTPUT, true, false)

    final GraphQLOperationType operationType
    final boolean embedded
    final boolean nested

    GraphQLPropertyType(GraphQLOperationType operationType, boolean embedded, boolean nested) {
        this.operationType = operationType
        this.embedded = embedded
        this.nested = nested
    }

    GraphQLPropertyType getEmbeddedType() {
        switch (operationType) {
            case GraphQLOperationType.OUTPUT:
                OUTPUT_EMBEDDED
                break
            case GraphQLOperationType.CREATE:
                CREATE_EMBEDDED
                break
            case GraphQLOperationType.UPDATE:
                UPDATE_EMBEDDED
                break
            default:
                throw new UnsupportedOperationException("No embedded type available for ${operationType.name()}")
        }
    }

    GraphQLPropertyType getNestedType() {
        switch (operationType) {
            case GraphQLOperationType.OUTPUT:
                OUTPUT
                break
            case GraphQLOperationType.CREATE:
                CREATE_NESTED
                break
            case GraphQLOperationType.UPDATE:
                UPDATE_NESTED
                break
            default:
                throw new UnsupportedOperationException("No nested type available for ${operationType.name()}")
        }
    }
}
