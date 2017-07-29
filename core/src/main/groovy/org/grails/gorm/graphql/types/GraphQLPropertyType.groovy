package org.grails.gorm.graphql.types

import groovy.transform.CompileStatic

/**
 * Represents what returnType of property is being created
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
enum GraphQLPropertyType {

    /**
     * For returning data
     */
    OUTPUT(GraphQLOperationType.OUTPUT),

    /**
     * For creating data
     */
    CREATE(GraphQLOperationType.CREATE),

    /**
     * For updating data (typically the same as create except nulls allowed)
     */
    UPDATE(GraphQLOperationType.UPDATE),

    /**
     * For supplying association data during a create
     */
    CREATE_NESTED(GraphQLOperationType.CREATE),

    /**
     * For supplying association data during an update
     */
    UPDATE_NESTED(GraphQLOperationType.UPDATE),

    /**
     * For creating embedded properties
     */
    CREATE_EMBEDDED(GraphQLOperationType.CREATE),

    /**
     * For updating embedded properties
     */
    UPDATE_EMBEDDED(GraphQLOperationType.UPDATE),

    /**
     * For displaying embedded properties
     */
    OUTPUT_EMBEDDED(GraphQLOperationType.OUTPUT)

    final GraphQLOperationType operationType

    GraphQLPropertyType(GraphQLOperationType operationType) {
        this.operationType = operationType
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
                throw new UnsupportedOperationException("No embedded returnType available for ${operationType.name()}")
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
                throw new UnsupportedOperationException("No nested returnType available for ${operationType.name()}")
        }
    }
}
