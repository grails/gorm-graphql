package grails.test.app

import grails.gorm.DetachedCriteria
import org.codehaus.groovy.util.HashCodeHelper
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping

class UserRole implements Serializable {

    private static final long serialVersionUID = 1

    User user
    Role role

    @Override
    boolean equals(other) {
        if (other instanceof UserRole) {
            other.userId == user?.id && other.roleId == role?.id
        }
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (user) {
            hashCode = HashCodeHelper.updateHash(hashCode, user.id)
        }
        if (role) {
            hashCode = HashCodeHelper.updateHash(hashCode, role.id)
        }
        hashCode
    }

    static boolean exists(long userId, long roleId) {
        criteriaFor(userId, roleId).count()
    }

    private static DetachedCriteria criteriaFor(long userId, long roleId) {
        UserRole.where {
            user == User.load(userId) &&
                    role == Role.load(roleId)
        }
    }



    static constraints = {
        role validator: { Role r, UserRole ur ->
            if (ur.user?.id) {
                UserRole.withNewSession {
                    if (UserRole.exists(ur.user.id, r.id)) {
                        return ['userRole.exists']
                    }
                }
            }
        }
    }

    static mapping = {
        id composite: ['user', 'role']
        version false
    }

    /**
     * The use of lazy here is required because the
     * data fetcher provided needs access to the persistent
     * entity API which will not be available when the
     * class is initialized
     */
    static graphql = GraphQLMapping.lazy {
        operations.update = false

        query('usersByRole') {
            argument('role', Long)
            type([String])
            dataFetcher(new UsersByRoleDataFetcher())
        }

        mutation('revokeAllRoles') {
            argument('user', Long)
            type([success: Boolean])
            dataFetcher(new RevokeAllRolesDataFetcher())
        }
    }
}
