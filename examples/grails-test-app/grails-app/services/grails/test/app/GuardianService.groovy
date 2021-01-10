package grails.test.app

import grails.gorm.services.Service
import grails.test.app.inheritance.Dog
import grails.test.app.unions.Guardian

@Service(Guardian)
interface GuardianService {
    Guardian save(String name)
}