package org.grails.gorm.graphql.entity

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType

/**
 * A class to return the names of class types and query/mutation names
 *
 * @author James Kleeh
 */
@CompileStatic
class GraphQLEntityNamingConvention {

    String getReadSingle(PersistentEntity entity) {
        entity.decapitalizedName
    }

    String getReadMany(PersistentEntity entity) {
        entity.decapitalizedName + "List"
    }

    String getCreate(PersistentEntity entity) {
        entity.decapitalizedName + "Create"
    }

    String getUpdate(PersistentEntity entity) {
        entity.decapitalizedName + "Update"
    }

    String getDelete(PersistentEntity entity) {
        entity.decapitalizedName + "Delete"
    }

    String getType(PersistentEntity entity, GraphQLPropertyType type) {
        final String simpleName = entity.javaClass.simpleName
        switch (type) {
            case GraphQLPropertyType.CREATE:
                simpleName + "Create"
                break
            case GraphQLPropertyType.UPDATE:
                simpleName + "Update"
                break
            case GraphQLPropertyType.INPUT_NESTED:
                simpleName + "InputNested"
                break
            default:
                simpleName
        }
    }
}
