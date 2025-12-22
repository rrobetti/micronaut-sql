/*
 * Copyright 2017-2025 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.jdbc.hikari

import io.micronaut.context.ApplicationContext
import io.micronaut.context.DefaultApplicationContext
import io.micronaut.context.env.MapPropertySource
import io.micronaut.jdbc.DataSourceResolver
import io.micronaut.jdbc.UnpooledDataSource
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection

class UnpooledOverridesHikariSpec extends Specification {

    void "test unpooled datasource is created instead of HikariCP when allow-unpooled=true"() {
        given:
        ApplicationContext applicationContext = new DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(MapPropertySource.of(
                'test',
                ['datasources.default.url': 'jdbc:h2:mem:hikaritest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
                 'datasources.default.driver-class-name': 'org.h2.Driver',
                 'datasources.default.username': 'sa',
                 'datasources.default.password': '',
                 'datasources.allow-unpooled': true]
        ))
        applicationContext.start()
        DataSourceResolver dataSourceResolver = applicationContext.findBean(DataSourceResolver).orElse(DataSourceResolver.DEFAULT)

        expect: "UnpooledDataSource is created, not HikariCP"
        applicationContext.containsBean(DataSource)
        !applicationContext.containsBean(DatasourceConfiguration)
        !applicationContext.containsBean(HikariUrlDataSource)

        when:
        DataSource dataSource = dataSourceResolver.resolve(applicationContext.getBean(DataSource))

        then: "The datasource is an UnpooledDataSource"
        dataSource instanceof UnpooledDataSource

        when: "Get two connections"
        Connection conn1 = dataSource.getConnection()
        Connection conn2 = dataSource.getConnection()

        then: "Each connection is different (unpooled behavior)"
        conn1 != conn2
        !conn1.is(conn2)

        cleanup:
        conn1?.close()
        conn2?.close()
        applicationContext.close()
    }

    void "test HikariCP is created when allow-unpooled is false or not set"() {
        given:
        ApplicationContext applicationContext = new DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(MapPropertySource.of(
                'test',
                ['datasources.default.url': 'jdbc:h2:mem:hikaritest2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
                 'datasources.default.driver-class-name': 'org.h2.Driver']
        ))
        applicationContext.start()

        expect: "HikariCP configuration is created"
        applicationContext.containsBean(DataSource)
        applicationContext.containsBean(DatasourceConfiguration)

        cleanup:
        applicationContext.close()
    }
}
