package org.samba.greeting;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
public class GreetingController {

    // TODO continue reading here: https://www.heise.de/hintergrund/Pragmatische-Kuechentricks-fuer-RESTful-HAL-APIs-4963049.html?seite=all

    private static final String TEMPLATE = "Hello, %s!";

//    public ResponseEntity greetingOverview() {
//         TODO this should give the two child relations
//                EntityModel.   like a root resource only containing children

//        new RepresentationModel();
//    }


    @GetMapping("/greeting/person")
    public ResponseEntity<Greeting> person(
            @RequestParam(value = "name", defaultValue = "World") String name) {

        var greeting = new Greeting(String.format(TEMPLATE, name));
        greeting.add(linkTo(methodOn(GreetingController.class).person(name)).withSelfRel());

        return new ResponseEntity<>(greeting, HttpStatus.OK);
    }

    @GetMapping("/greeting/hello")
    public ResponseEntity<EntityModel<String>> hello() {
        EntityModel<String> response = EntityModel.of(
                "Hello there",
                linkTo(methodOn(GreetingController.class).hello()).withSelfRel());
        return ResponseEntity.ok(response);
    }
}