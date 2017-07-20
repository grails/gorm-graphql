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

    static graphql = GraphQLMapping.build {
        operations.update = false
    }
}
