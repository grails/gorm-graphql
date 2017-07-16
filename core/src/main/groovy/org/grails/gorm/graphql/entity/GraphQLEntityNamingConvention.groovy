package org.grails.gorm.graphql.entity

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType

/**
 * A class to return the names of class types and query/mutation names
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLEntityNamingConvention {

    /**
     * @param entity The persistent entity
     * @return The name to use. Ex: "person"
     */
    String getGet(PersistentEntity entity) {
        entity.decapitalizedName
    }

    /**
     * @param entity The persistent entity
     * @return The name to use. Ex: "personList"
     */
    String getList(PersistentEntity entity) {
        entity.decapitalizedName + 'List'
    }

    /**
     * @param entity The persistent entity
     * @return The name to use. Ex: "personCreate"
     */
    String getCreate(PersistentEntity entity) {
        entity.decapitalizedName + 'Create'
    }

    /**
     * @param entity The persistent entity
     * @return The name to use. Ex: "personUpdate"
     */
    String getUpdate(PersistentEntity entity) {
        entity.decapitalizedName + 'Update'
    }

    /**
     * @param entity The persistent entity
     * @return The name to use. Ex: "personDelete"
     */
    String getDelete(PersistentEntity entity) {
        entity.decapitalizedName + 'Delete'
    }

    /**
     * @param entity The persistent entity
     * @param type The property type
     * @return The name to use. Ex: "Person", "PersonCreate", "PersonUpdate", "PersonInputNested"
     */
    String getType(PersistentEntity entity, GraphQLPropertyType type) {
        final String SIMPLE_NAME = entity.javaClass.simpleName
        switch (type) {
            case GraphQLPropertyType.CREATE:
                SIMPLE_NAME + 'Create'
                break
            case GraphQLPropertyType.UPDATE:
                SIMPLE_NAME + 'Update'
                break
            case GraphQLPropertyType.INPUT_NESTED:
                SIMPLE_NAME + 'InputNested'
                break
            default:
                SIMPLE_NAME
        }
    }
}
