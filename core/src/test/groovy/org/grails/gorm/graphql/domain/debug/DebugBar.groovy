package org.grails.gorm.graphql.domain.debug

import grails.gorm.annotation.Entity

@Entity
class DebugBar {

    DebugFoo foo
    DebugCircular circular

    //assert any static property that matches "graphql" ignoring case works
    static GrApHQl = true
}
