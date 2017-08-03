package org.grails.gorm.graphql.plugin

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import org.grails.plugins.databinding.DataBindingGrailsPlugin
import org.springframework.boot.context.properties.ConfigurationProperties

import javax.annotation.PostConstruct

@CompileStatic
@ConfigurationProperties(prefix = 'grails.gorm.graphql')
class GrailsGraphQLConfiguration implements GrailsConfigurationAware {

    private Config config

    Boolean enabled = true

    List<String> dateFormats

    Boolean dateFormatLenient

    Map<String, Class> listArguments

    Boolean browser

    @Override
    void setConfiguration(Config co) {
        config = co
    }

    @PostConstruct
    void init() {
        if (dateFormats == null) {
            dateFormats = config.getProperty('grails.databinding.dateFormats', List, DataBindingGrailsPlugin.DEFAULT_DATE_FORMATS)
        }
        if (dateFormatLenient == null) {
            dateFormatLenient = config.getProperty('grails.databinding.dateParsingLenient', Boolean, false)
        }
        if (browser == null) {
            browser = false
        }
    }
}
