package grails.docs.app

import demo.Speaker
import demo.Talk

class BootStrap {

    def init = { servletContext ->


        Speaker jeffScottBrown = new Speaker(firstName: 'Jeff Scott', lastName: 'Brown')
        jeffScottBrown.email = 'jeffScottBrown@email.com'
        jeffScottBrown.bio = 'Jeff is a co-founder of the Grails framework, and a core member of the Grails development team.'
        jeffScottBrown.addToTalks(new Talk(title: 'Polyglot Web Development with Grails 3', duration: 50))
        jeffScottBrown.addToTalks(new Talk(title: 'REST With Grails 3', duration: 50))
        jeffScottBrown.addToTalks(new Talk(title: 'Testing in Grails 3', duration: 50))
        jeffScottBrown.save()

        Speaker graemeRocher = new Speaker(firstName: 'Graeme', lastName: 'Rocher')
        graemeRocher.email = 'graemeRocher@email.com'
        graemeRocher.bio = 'Graeme is a co-founder of the Grails framework and co-authored “The Definitive Guide to Grails” - Apress.'
        graemeRocher.addToTalks(new Talk(title: 'What\'s New in Grails?', duration: 50))
        graemeRocher.addToTalks(new Talk(title: 'The Latest and Greatest in GORM', duration: 50))
        graemeRocher.save()

        Speaker paulKing = new Speaker(firstName: 'Paul', lastName: 'King')
        paulKing.email = 'paulKing@email.com'
        paulKing.bio = 'Paul King has been contributing to Open Source projects for nearly 30 years and is an active committer on numerous projects, including Groovy, GPars, and Gradle. Paul speaks at international conferences, publishes in software magazines and journals, and is a co-author of Manning’s best-seller, Groovy in Action, 2nd Edition.'
        paulKing.addToTalks(new Talk(title: 'Groovy: The Awesome Parts', duration: 50))
        paulKing.save()

        Speaker ivanLopez = new Speaker(firstName: 'Iván', lastName: 'López')
        ivanLopez.email = 'ivanLopez@email.com'
        ivanLopez.bio = "Iván discovered Grails 7 years ago and since almost exclusively develops using Groovy. He is the creator of some Grails plugins like Postgresql-Extensions and Slug-Generator. He's also the coordinator of the Madrid Groovy User Group and the organizer of the Greach Conference."
        ivanLopez.addToTalks(new Talk(title: 'From Java to Groovy: Adventure time!', duration: 50))
        ivanLopez.addToTalks(new Talk(title: 'Dockerize your Grails!', duration: 50))
        ivanLopez.save()

        Speaker ryanVanderwerf = new Speaker(firstName: 'Ryan', lastName: 'Vanderwerf')
        ryanVanderwerf.email = 'ryanVanderwerf@email.com'
        ryanVanderwerf.bio = 'Ryan is a developer on the core Grails team. Formerly, he served as the Chief Systems and Software Architect and Director of Products at ReachForce and Lead Architect at www.developerprogram.com. He has helped maintain various Grails plugins and serves as co-chair of the Groovy and Grails User Group in Austin, TX.'
        ryanVanderwerf.addToTalks(new Talk(title: 'Alexa, Tell Me I\'m Groovy!', duration: 50))
        ryanVanderwerf.addToTalks(new Talk(title: 'Getting Groovy with Google Home', duration: 50))
        ryanVanderwerf.addToTalks(new Talk(title: 'Amazon Alexa Workshop', duration: 180))
        ryanVanderwerf.save()

        Speaker colinHarrington = new Speaker(firstName: 'Colin', lastName: 'Harrington')
        colinHarrington.email = 'colinHarrington@email.com'
        colinHarrington.bio = '''
Colin boasts over 7 years of Grails experience and 10+ years of experience developing web-based applications. He's an agile practitioner with a proven track record having been a key component of multiple powerful fast-paced teams
'''
        colinHarrington.addToTalks(new Talk(title: 'Performance tuning your Grails apps', duration: 50))
        colinHarrington.addToTalks(new Talk(title: 'Grails and Docker', duration: 50))
        colinHarrington.save()

        Speaker zacharyKlein = new Speaker(firstName: 'Zachary', lastName: 'Klein')
        zacharyKlein.email = 'colinHarrington@email.com'
        zacharyKlein.bio = '''
Zacahry has been doing Groovy & Grails development for over 6 years. Outside of the JVM and web dev space, Zachary is the author of the webpack and React profiles for Grails 3. 
'''
        zacharyKlein.addToTalks(new Talk(title: 'Grails and the Wonderful World of Javascript Frameworks', duration: 50))
        zacharyKlein.addToTalks(new Talk(title: 'Using React with Grails 3', duration: 50))
        zacharyKlein.save()
    }
    def destroy = {
    }
}
