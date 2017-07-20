package grails.test.app

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import org.grails.web.json.JSONObject
import org.spockframework.util.StringMessagePrintStream
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Integration
@Stepwise
class UserRoleIntegrationSpec extends Specification implements GraphQLSpec {

    @Shared Long userId
    @Shared Long roleId

    @OnceBefore
    void createUserAndRole() {
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "admin@email.com",
                        firstName: "First",
                        lastName: "Last"
                    }
                    address: {
                        city: "Youngstown",
                        state: "OH",
                        zip: 44512
                    }
                }) {
                    id
                }
            }
        """)
        JSONObject obj = resp.json.data.userCreate
        userId = obj.id

        resp = graphQL.graphql("""
            mutation {
                roleCreate(role: {
                    authority: "ROLE_ADMIN"
                }) {
                    id
                }
            }
        """)
        obj = resp.json.data.roleCreate
        roleId = obj.id
    }

    void "test creating an entity with a complex composite id"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userRoleCreate(userRole: {
                    user: {
                        id: ${userId}
                    },
                    role: {
                        id: ${roleId}
                    }
                }) {
                    user {
                        profile {
                            email
                        }
                    }
                    role {
                        authority
                    }
                }
            }
        """)
        JSONObject obj = resp.json.data.userRoleCreate

        then:
        obj.user.profile.email == 'admin@email.com'
        obj.role.authority == 'ROLE_ADMIN'
    }

    void "test reading an entity with a complex composite id"() {
        given:
        PrintStream originalOut = System.out
        String query
        int outCount = 0
        System.setOut(new StringMessagePrintStream() {
            @Override
            protected void printed(String message) {
                query = message
                outCount++
            }
        })

        when:
        def resp = graphQL.graphql("""
            {
                userRole(role: ${roleId}, user: ${userId}) {
                    user {
                        id
                    }
                    role {
                        id
                    }
                }
            }
        """.toString())
        JSONObject obj = resp.json.data.userRole

        then:
        obj.user.id == userId
        obj.role.id == roleId
        outCount == 1
        query == 'Hibernate: select this_.user_id as user_id1_12_0_, this_.role_id as role_id2_12_0_ from user_role this_ where this_.user_id=? and this_.role_id=? limit ?\n'

        when:
        outCount = 0
        resp = graphQL.graphql("""
            {
                userRole(role: ${roleId}, user: ${userId}) {
                    user {
                        profile {
                            email
                        }
                    }
                    role {
                        authority
                    }
                }
            }
        """.toString())
        obj = resp.json.data.userRole

        then: 'The user and role will be fetched with the same query'
        obj.user.profile.email == 'admin@email.com'
        obj.role.authority == 'ROLE_ADMIN'
        outCount == 1
        query == 'Hibernate: select this_.user_id as user_id1_12_2_, this_.role_id as role_id2_12_2_, user2_.id as id1_11_0_, user2_.version as version2_11_0_, user2_.manager_id as manager_3_11_0_, user2_.added_numbers as added_nu4_11_0_, user2_.address_zip as address_5_11_0_, user2_.address_city as address_6_11_0_, user2_.address_state as address_7_11_0_, user2_.profile_first_name as profile_8_11_0_, user2_.profile_last_name as profile_9_11_0_, user2_.profile_email as profile10_11_0_, role3_.id as id1_8_1_, role3_.version as version2_8_1_, role3_.authority as authorit3_8_1_ from user_role this_ inner join user user2_ on this_.user_id=user2_.id inner join role role3_ on this_.role_id=role3_.id where this_.user_id=? and this_.role_id=?\n'

        cleanup:
        System.setOut(originalOut)
    }

    void "test listing entities with a complex composite id"() {

    }
    /*
    void "test updating an entity with a simple composite id"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                simpleCompositeUpdate(title: "x", description: "y", simpleComposite: {
                    someUUID: "8e22054f-a419-44dd-8726-1e53023cb7be"
                }) {
                    title
                    description
                    someUUID
                }
            }
        """)
        JSONObject obj = resp.json.data.simpleCompositeUpdate

        then:
        obj.title == 'x'
        obj.description == 'y'
        obj.someUUID == '8e22054f-a419-44dd-8726-1e53023cb7be'
    }

    void "test retrieving an entity with a simple composite id"() {
        when:
        def resp = graphQL.graphql("""
            {
                simpleComposite(title: "x", description: "y") {
                    title
                    description
                    someUUID
                }
            }
        """)
        JSONObject obj = resp.json.data.simpleComposite

        then:
        obj.title == 'x'
        obj.description == 'y'
        obj.someUUID == '8e22054f-a419-44dd-8726-1e53023cb7be'
    }

    void "test listing entities with a simple composite id"() {
        when:
        def resp = graphQL.graphql("""
            {
                simpleCompositeList {
                    title
                    description
                    someUUID
                }
            }
        """)
        JSONArray obj = resp.json.data.simpleCompositeList

        then:
        obj.size() == 1
        obj[0].title == 'x'
        obj[0].description == 'y'
        obj[0].someUUID == '8e22054f-a419-44dd-8726-1e53023cb7be'
    }

    void "test deleting an entity with a simple composite id"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                simpleCompositeDelete(title: "x", description: "y") {
                    success
                }
            }
        """)
        JSONObject obj = resp.json.data.simpleCompositeDelete

        then:
        obj.success
    }
    */
}
