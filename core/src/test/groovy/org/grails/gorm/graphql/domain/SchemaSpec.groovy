package org.grails.gorm.graphql.domain

import graphql.schema.*
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.gorm.graphql.Schema
import org.grails.orm.hibernate.HibernateDatastore
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

//@Ignore('Until the rest of the basic graphql types are created')
class SchemaSpec extends Specification {

    @Shared @AutoCleanup HibernateDatastore hibernateDatastore
    @Shared GraphQLSchema schema
    @Shared GraphQLObjectType queryType
    @Shared GraphQLObjectType mutationType

    void setupSpec() {
        hibernateDatastore = new HibernateDatastore(
                DatastoreUtils.createPropertyResolver(Collections.singletonMap(Settings.SETTING_DB_CREATE, 'create-drop')),
                getClass().classLoader.getPackage('org.grails.gorm.graphql.domain'))

        schema = new Schema(hibernateDatastore.mappingContext).generate()
        queryType = schema.queryType
        mutationType = schema.mutationType
    }

    void notNull(GraphQLType type, GraphQLType expectedType) {
        type instanceof GraphQLNonNull && ((GraphQLNonNull)type).wrappedType == expectedType
    }

    void list(GraphQLType type, GraphQLType expectedType) {
        type instanceof GraphQLList && ((GraphQLList)type).wrappedType == expectedType
    }

    void "test it"() {
        expect:
        1 == 1
    }

}
