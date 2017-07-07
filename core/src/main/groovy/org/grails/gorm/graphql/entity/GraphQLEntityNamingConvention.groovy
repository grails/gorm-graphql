package org.grails.gorm.graphql.entity

import org.grails.datastore.mapping.model.PersistentEntity

/**
 * Created by jameskleeh on 7/7/17.
 */
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
