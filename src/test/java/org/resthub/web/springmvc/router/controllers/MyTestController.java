package org.resthub.web.springmvc.router.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MyTestController {

    public void simpleAction() {
    }

    public void additionalRouteFile() {
    }

    public void wildcardA() {
    }

    public void wildcardB() {
    }

    public void caseInsensitive() {
    }

    public void overrideMethod() {
    }

    public void paramAction(@PathVariable(value = "param") String param) {
    }

    public void httpAction(@PathVariable(value = "type") String type) {
    }

    public void regexNumberAction(@PathVariable(value = "number") int number) {
    }

    public void regexStringAction(@PathVariable(value = "string") String string) {
    }

    public void hostAction(@PathVariable(value = "host") String host) {
    }
}
