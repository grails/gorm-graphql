package org.grails.gorm.graphql.entity

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity

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

    String getOutputType(PersistentEntity entity) {
        entity.javaClass.simpleName
    }

    String getCreateType(PersistentEntity entity) {
        entity.javaClass.simpleName + "Create"
    }

    String getUpdateType(PersistentEntity entity) {
        entity.javaClass.simpleName + "Update"
    }
}
