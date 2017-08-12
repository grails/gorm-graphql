package org.grails.gorm.graphql.domain.debug

import grails.gorm.annotation.Entity

@Entity
class DebugFoo {

    DebugBar bar

    static hasMany = [items: DebugFooItem]

}
