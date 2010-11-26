package org.springframework.web.servlet.mvc.router;

import javax.inject.Named;
import org.springframework.security.access.annotation.Secured;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Named("bindTestController")
public class BindTestController {

    public ModelAndView bindNameAction(@PathVariable(value = "myName") String myName) {

        ModelAndView mav = new ModelAndView("testView");
        mav.addObject("name", myName);

        return mav;
    }

    public ModelAndView bindIdAction(@PathVariable(value = "myId") Long myId) {

        ModelAndView mav = new ModelAndView("testView");
        mav.addObject("id", myId);

        return mav;

    }

    public ModelAndView bindSlugAction(@PathVariable(value = "slug") String mySlug,
            @RequestParam(value = "hash", required = true) String myHash) {

        ModelAndView mav = new ModelAndView("testView");
        mav.addObject("slug", mySlug);
        mav.addObject("hash", myHash);

        return mav;
    }

    public ModelAndView bindHostAction(@PathVariable(value = "host") String myHost) {

        ModelAndView mav = new ModelAndView("testView");
        mav.addObject("host", myHost);

        return mav;
    }

    public ModelAndView bindSpecificHostAction() {

        ModelAndView mav = new ModelAndView("testView");
        mav.addObject("host", "specific");

        return mav;
    }

    public ModelAndView bindRegexpHostAction(@PathVariable(value = "subdomain") String subdomain) {
    //public ModelAndView bindRegexpHostAction() {
        ModelAndView mav = new ModelAndView("testView");
        mav.addObject("subdomain", subdomain);

        return mav;
    }
    
    @Secured("ROLE_ADMIN")
    public ModelAndView securityAction(@PathVariable(value = "name") String name) {

        ModelAndView mav = new ModelAndView("testView");
        mav.addObject("name", name);

        return mav;
    }
}
