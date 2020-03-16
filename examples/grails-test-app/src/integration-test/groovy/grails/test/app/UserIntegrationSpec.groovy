package grails.test.app

import org.grails.gorm.graphql.plugin.testing.GraphQLSpec
import grails.testing.mixin.integration.Integration
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Integration
@Stepwise
class UserIntegrationSpec extends Specification implements GraphQLSpec {

    @Shared Long lastId

    void "test creating a user without a profile"() {
        given:
        User.withNewSession {
            lastId = User.last()?.id ?: 0
        }

        when:
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
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
        Map obj = resp.body()

        then:
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')

        when: 'The profile is provided, but missing a required field'
        resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "email",
                        firstName: "First"
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
        obj = resp.body()

        then:
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')
    }

    void "test creating a user without an address"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "email",
                        firstName: "First",
                        lastName: "Last"
                    }
                }) {
                    id
                }
            }
        """)
        Map obj = resp.body()

        then:
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')

        when: 'The address is provided, but missing a required field'
        resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "email",
                        firstName: "First",
                        lastName: "Last"
                    },
                    address: {
                        city: "Youngstown",
                        state: "OH"
                    }
                }) {
                    id
                }
            }
        """)
        obj = resp.body()

        then:
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')
    }

    void "test creating the top level manager"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "email",
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
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                    }
                }
            }
        """)
        Map obj = resp.body().data.userCreate

        then:
        obj.id == lastId + 1
        obj.addedNumbers == 5
        obj.profile.email == 'email'
        obj.profile.firstName == 'First'
        obj.profile.lastName == 'Last'
        obj.address.city == 'Youngstown'
        obj.address.state == 'OH'
        obj.address.zip == 44512
        obj.manager == null
    }

    void "create new user with manager"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 4,
                    secondNumber: 7,
                    profile: {
                        email: "email",
                        firstName: "First",
                        lastName: "Last"
                    }
                    address: {
                        city: "Youngstown",
                        state: "OH",
                        zip: 44512
                    }
                    manager: {
                        id: ${lastId + 1}
                    }
                }) {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                    }
                }
            }
        """)
        Map obj = resp.body().data.userCreate

        then:
        obj.id == lastId + 2
        obj.addedNumbers == 11
        obj.profile.email == 'email'
        obj.profile.firstName == 'First'
        obj.profile.lastName == 'Last'
        obj.address.city == 'Youngstown'
        obj.address.state == 'OH'
        obj.address.zip == 44512
        obj.manager.id == lastId + 1
    }

    void "test updating a user"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userUpdate(id: ${lastId + 2}, user: {
                    firstNumber: 5,
                    secondNumber: 7,
                    profile: {
                        email: "emailUpdated",
                        firstName: "FirstUpdated",
                        lastName: "LastUpdated"
                    }
                    address: {
                        city: "Pittsburgh",
                        state: "PA",
                        zip: 90210
                    }
                }) {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                    }
                }
            }
        """)
        Map obj = resp.body().data.userUpdate

        then:
        obj.id == lastId + 2
        obj.addedNumbers == 12
        obj.profile.email == 'emailUpdated'
        obj.profile.firstName == 'FirstUpdated'
        obj.profile.lastName == 'LastUpdated'
        obj.address.city == 'Pittsburgh'
        obj.address.state == 'PA'
        obj.address.zip == 90210
        obj.manager.id == lastId + 1
    }

    void "test listing users"() {
        when:
        def resp = graphQL.graphql("""
            {
                userList(sort: "id") {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                        addedNumbers
                    }
                }
            }
        """)
        List obj = resp.body().data.userList

        then:
        obj.size() == lastId + 2

        obj[lastId].id == lastId + 1
        obj[lastId].addedNumbers == 5
        obj[lastId].profile.email == 'email'
        obj[lastId].profile.firstName == 'First'
        obj[lastId].profile.lastName == 'Last'
        obj[lastId].address.city == 'Youngstown'
        obj[lastId].address.state == 'OH'
        obj[lastId].address.zip == 44512
        obj[lastId].manager == null

        obj[lastId + 1].id == lastId + 2
        obj[lastId + 1].addedNumbers == 12
        obj[lastId + 1].profile.email == 'emailUpdated'
        obj[lastId + 1].profile.firstName == 'FirstUpdated'
        obj[lastId + 1].profile.lastName == 'LastUpdated'
        obj[lastId + 1].address.city == 'Pittsburgh'
        obj[lastId + 1].address.state == 'PA'
        obj[lastId + 1].address.zip == 90210
        obj[lastId + 1].manager.id == lastId + 1
        obj[lastId + 1].manager.addedNumbers == 5
    }

    void "test querying a single user"() {
        when:
        def resp = graphQL.graphql("""
            {
                user(id: ${lastId + 2}) {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                        addedNumbers
                    }
                }
            }
        """)
        Map json = resp.body()
        JSONObject obj = json.data.user

        then:
        obj.id == lastId + 2
        obj.addedNumbers == 12
        obj.profile.email == 'emailUpdated'
        obj.profile.firstName == 'FirstUpdated'
        obj.profile.lastName == 'LastUpdated'
        obj.address.city == 'Pittsburgh'
        obj.address.state == 'PA'
        obj.address.zip == 90210
        obj.manager.id == lastId + 1
        obj.manager.addedNumbers == 5
    }

    void "test deleting a user that is a manager of another user"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userDelete(id: ${lastId + 1}) {
                    success
                }
            }
        """)
        Map obj = resp.body().data.userDelete

        then:
        !obj.success
    }

    void "test deleting a user that is NOT a manager of another user"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userDelete(id: ${lastId + 2}) {
                    success
                }
            }
        """)
        Map obj = resp.body().data.userDelete

        then:
        obj.success
    }

    void "test deleting the last user is now successful"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userDelete(id: ${lastId + 1}) {
                    success
                }
            }
        """)
        Map obj = resp.body().data.userDelete

        then:
        obj.success
    }
}
