package org.resthub.web.springmvc.router;

import javax.inject.Named;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@Named("myTestController")
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
    
    
    // querystring params tests
    
    public void qsParamPresence() {
    }
    
    public void qsParamNegatePresence() {
    }
    
    public void qsParamEmptyValueRequired() {
    }
    
    public void qsParamSpecificValueRequired() {
    }
    
    public void qsParamSpecificValueRequiredPost() {
    }
    
    public void qsParamNegateSpecificValue() {
    }
    
    public void qsParamTwoParamsRequired() {
    }
    
    public void qsParamEncodedValueAndRandomSpaces() {
    }
    
    public void qsParamPlayNiceWithOtherRoutingFeatures(@PathVariable(value = "myName") String myName) {
    }
    
}
