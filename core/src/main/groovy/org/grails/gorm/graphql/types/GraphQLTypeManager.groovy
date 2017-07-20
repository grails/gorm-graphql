package org.grails.gorm.graphql.types

import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLType
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention

/**
 * An interface for handling type conversion and creation with GraphQL
 *
 * @author James Kleeh
 * @since 1.0.0
 */
interface GraphQLTypeManager {

    /**
     * Retrieves the corresponding GraphQL type for the specified class.
     * This method should typically return a {@link graphql.schema.GraphQLScalarType}
     *
     * @param clazz The class to retrieve a type for
     * @return The GraphQLType
     */
    GraphQLType getType(Class clazz) throws TypeNotFoundException

    /**
     * Retrieves whether or not a GraphQL type has been registered for the
     * provided class
     *
     * @param clazz The class to search for a type for
     * @return True if a type was found
     */
    boolean hasType(Class clazz)

    /**
     * Retrieves the corresponding GraphQL type for the specified class.
     * This method should typically return a {@link graphql.schema.GraphQLScalarType}
     *
     * @param clazz The class to retrieve a type for
     * @param nullable If true, wrap the normal result with a {@link graphql.schema.GraphQLNonNull}
     * @return The GraphQLType
     */
    GraphQLType getType(Class clazz, boolean nullable)

    /**
     * Register a GraphQL type to represent the provided class
     *
     * @param clazz The class the type represents
     * @param type The type
     */
    void registerType(Class clazz, GraphQLType type)

    /**
     * @return The naming convention used to name types
     */
    GraphQLEntityNamingConvention getNamingConvention()

    /**
     * Retrieves an enum type for the provided class
     *
     * @param clazz The clazz to create
     * @param nullable True if the property allows nulls
     * @return The type representing the provided enum
     */
    GraphQLType getEnumType(Class<? extends Enum> clazz, boolean nullable)

    /**
     * Creates a reference to domain type
     *
     * @param entity The entity to reference
     * @param type The type of reference
     * @return The domain reference
     */
    GraphQLType createReference(PersistentEntity entity, GraphQLPropertyType type)

    /**
     * Retrieves a GraphQL type used for mutations that represents the provided entity
     *
     * @param entity The persistent entity to retrieve the type for
     * @param type The type of property to retrieve
     * @param nullable True if the property allows nulls
     * @return The type representing the provided entity
     */
    GraphQLInputType getMutationType(PersistentEntity entity, GraphQLPropertyType type, boolean nullable)

    /**
     * Retrieves a GraphQL type used for queries that represents the provided entity
     *
     * @param entity The persistent entity to retrieve the type for
     * @param type The type of property to retrieve
     * @return The type representing the provided entity
     */
    GraphQLOutputType getQueryType(PersistentEntity entity, GraphQLPropertyType type)

}
