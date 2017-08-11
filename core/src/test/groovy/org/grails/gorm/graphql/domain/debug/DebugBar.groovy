package org.grails.gorm.graphql.domain.debug

import grails.gorm.annotation.Entity

@Entity
class DebugBar {

    DebugFoo foo
    DebugCircular circular

    static graphql = true
}
