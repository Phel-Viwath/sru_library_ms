/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.College;
import sru.edu.sru_lib_management.core.domain.service.CollegeService;

@Controller
@RequestMapping("/api/v1/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;

    @PostMapping
    public Mono<ResponseEntity<Object>> addNewCollege(@RequestBody College college){
        return collegeService.save(college)
                .map(result -> ResponseEntity.accepted().body(result))
                .onErrorResume(this::handleError);
    }

    @GetMapping
    @ResponseBody
    public Flux<College> getAllCollege(){
        return collegeService.findAll().onErrorResume(e -> {
            e.printStackTrace();
            return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getById(@PathVariable String id){
        return collegeService.findById(id)
                .map(result -> ResponseEntity.status(200).body(result))
                .onErrorResume(this::handleError);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateCollege(@RequestBody College college, @PathVariable String id){
        return collegeService.update(college, id)
                .map(result -> ResponseEntity.accepted().body(result))
                .onErrorResume(this::handleError);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteCollege(@PathVariable String id) {
        return collegeService.delete(id)
                .map(result -> result
                        ? ResponseEntity.ok().body("College deleted successfully")
                        : ResponseEntity.status(404).body("College not found")
                )
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

    private Mono<ResponseEntity<Object>> handleError(Throwable e) {
        if (e instanceof ResponseStatusException rse) {
            return Mono.just(ResponseEntity.status(rse.getStatusCode()).body(rse.getReason()));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
        }
    }
}
