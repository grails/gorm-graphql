package org.grails.gorm.graphql

import com.github.fakemongo.Fongo
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLSchema
import org.bson.types.ObjectId
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.gorm.graphql.domain.general.GeneralPackage
import org.grails.gorm.graphql.testing.GraphQLSchemaSpec
import org.grails.gorm.graphql.types.GraphQLTypeManager
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class MongoSchemaSpec extends Specification implements GraphQLSchemaSpec {

    @Shared @AutoCleanup MongoDatastore mongoDatastore
    @Shared GraphQLSchema schema
    @Shared GraphQLObjectType queryType
    @Shared GraphQLObjectType mutationType

    void setupSpec() {
        mongoDatastore = new MongoDatastore(new Fongo(getClass().name).mongo,
                DatastoreUtils.createPropertyResolver(Collections.emptyMap()),
                GeneralPackage.getPackage())

        def gormSchema = new Schema(mongoDatastore.mappingContext)
        gormSchema.initialize()
        GraphQLTypeManager typeManager = gormSchema.typeManager
        // tag::registerObjectId[]
typeManager.registerType(ObjectId, new GraphQLScalarType("ObjectId", "Hex representation of a Mongo object id", new Coercing<ObjectId, ObjectId>() {

    @Override
    ObjectId serialize(Object input) {
        if (input instanceof ObjectId) {
            (ObjectId) input
        }
        else {
            null
        }
    }

    @Override
    ObjectId parseValue(Object input) {
        serialize(input)
    }

    @Override
    ObjectId parseLiteral(Object input) {
        if (input instanceof StringValue) {
            new ObjectId(((StringValue) input).value)
        }
        else {
            null
        }
    }

}))
        // end::registerObjectId[]
        schema = gormSchema.generate()
        queryType = schema.queryType
        mutationType = schema.mutationType
    }

    void "test it doesn't throw any exceptions"() {
        expect:
        1 == 1
    }
}
