/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.config.EnableWebFlux


@SpringBootApplication
@EnableScheduling
@EnableWebFlux
class Application

fun main(args: Array<String>){
    runApplication<Application>(*args)
}
