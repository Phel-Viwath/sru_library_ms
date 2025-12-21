package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.StudentHandler

@Configuration
class StudentRoute {
    @Bean
    @FlowPreview
    fun studentRoute(studentHandler: StudentHandler): RouterFunction<ServerResponse> = coRouter {
        accept(APPLICATION_JSON).nest {
            "api/v1/student".nest {
                GET(""){studentHandler.getAllStudents()}
                GET("/{studentId}", studentHandler::getStudentById)
                POST("", studentHandler::saveStudent)
                DELETE("/delete/{studentId}", studentHandler::deleteStudent)
                PUT("/{studentId}", studentHandler::updateStudent)
            }
        }
    }
}