package myapp

import grails.testing.mixin.integration.Integration
import org.grails.datastore.gorm.GormEnhancer
import org.grails.gorm.graphql.plugin.testing.GraphQLSpec
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.web.json.JSONObject
import spock.lang.Specification

@Integration
class FooIntegrationSpec extends Specification implements GraphQLSpec {

    void "test a foo can be created"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                fooCreate(foo: {name: "x"}) {
                    id
                    errors {
                        field
                        message
                    }
                }
            }
        """)
        JSONObject obj = resp.json.data.fooCreate

        then:
        obj.id == 1
        GormEnhancer.findStaticApi(Foo).datastore instanceof HibernateDatastore
    }
}
