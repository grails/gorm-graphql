package org.grails.gorm.graphql.domain.debug

import grails.gorm.annotation.Entity

@Entity
class DebugCircular {

    DebugCircular otherCircular

    static hasMany = [circulars: DebugCircular]

    static belongsTo = [parentCircular: DebugCircular]

}
