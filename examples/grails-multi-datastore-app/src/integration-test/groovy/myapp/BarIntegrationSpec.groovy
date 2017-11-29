package myapp

import grails.testing.mixin.integration.Integration
import org.bson.types.ObjectId
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.gorm.graphql.plugin.testing.GraphQLSpec
import org.grails.web.json.JSONObject
import spock.lang.Specification

@Integration
class BarIntegrationSpec extends Specification implements GraphQLSpec {

    void "test a bar can be created"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                barCreate(bar: {name: "x"}) {
                    id
                    errors {
                        field
                        message
                    }
                }
            }
        """)
        JSONObject obj = resp.json.data.barCreate

        then:
        new ObjectId((String) obj.id)
        GormEnhancer.findStaticApi(Bar).datastore instanceof MongoDatastore
    }
}
