package org.resthub.web.springmvc.router;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import static org.fest.assertions.api.Assertions.*;
import org.resthub.web.springmvc.router.Router.ActionDefinition;
import org.resthub.web.springmvc.router.exceptions.NoHandlerFoundException;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath*:routerTestContext.xml"})
public class ReverseRoutingTest extends AbstractTestNGSpringContextTests {

    private String LOCATION = "/routerTestContext.xml";
    private XmlWebApplicationContext wac;
    private HandlerMapping hm;

    /**
     * Setup a MockServletContext configured by routerTestContext.xml
     */
    @BeforeClass
    public void setUp() {

        MockServletContext sc = new MockServletContext("");
        this.wac = new XmlWebApplicationContext();
        this.wac.setServletContext(sc);
        this.wac.setConfigLocations(new String[]{LOCATION});
        this.wac.refresh();

        this.hm = (HandlerMapping) this.wac.getBean("handlerMapping");
    }

    @Test
    public void testSimpleReverse() {

        ActionDefinition action = Router.reverse("myTestController.simpleAction");

        assertThat(action).isNotNull();
        assertThat(action.url).isEqualTo("/simpleaction");
    }

    @Test
    public void testParamReverse() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("param", "testparam");
        ActionDefinition action = Router.reverse("myTestController.paramAction", params);

        assertThat(action).isNotNull();
        assertThat(action.url).isEqualTo("/param/" + params.get("param"));
    }

    @Test
    public void testRegexpParamReverse() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("slug", "slug_01");
        ActionDefinition action = Router.reverse("bindTestController.bindSlugAction", params);

        assertThat(action).isNotNull();
        assertThat(action.url).isEqualTo("/bind/slug/" + params.get("slug"));
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void testFailRegexpReverse() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "name01");
        Router.reverse("bindTestController.bindNameAction", params);

    }

    @Test
    public void testHostRegexpReverse() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("subdomain", "sub");
        ActionDefinition action = Router.reverse("bindTestController.bindRegexpHostAction", params);

        assertThat(action).isNotNull();
        assertThat(action.url).isEqualTo("/bind/regexphost");
        assertThat(action.host).isEqualTo("sub.domain.org");
    }

    @Test
    public void testHostSlugReverse() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("slug", "slug1");
        ActionDefinition action = Router.reverse("bindTestController.bindHostSlugAction", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/bind/hostslug/slug1");

    }

    //--------------------------------
    // querystring params tests
    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamPresenceReverseFail() {

        // should fail because no request param is given
        Router.reverse("myTestController.qsParamPresence");
    }

    @Test
    public void qsParamPresenceReverse() {

        // with the qsparam : should work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "");
        ActionDefinition action = Router.reverse("myTestController.qsParamPresence", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparampresence?qsParamA=");
    }

    @Test
    public void qsParamNegatePresenceReverse() {

        // without the qsparam : should work
        Map<String, Object> params = new HashMap<String, Object>();
        ActionDefinition action = Router.reverse("myTestController.qsParamNegatePresence", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparamnegatepresence");
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamNegatePresenceReverseFail() {
        // with the qsparam, should fail
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");

        Router.reverse("myTestController.qsParamNegatePresence", params);
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamEmptyValueRequiredReverseFail() {

        Map<String, Object> params = new HashMap<String, Object>();
        // without the qsparam : should fail
        Router.reverse("myTestController.qsParamEmptyValueRequired", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamEmptyValueRequiredReverseWrongValue() {

        // wrong qsparam's value : should fail
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "wrongValue");

        Router.reverse("myTestController.qsParamEmptyValueRequired", params);

    }

    @Test
    public void qsParamEmptyValueRequiredReverse() {

        //  qsparam with empty value: should work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "");
        ActionDefinition action = Router.reverse("myTestController.qsParamEmptyValueRequired", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparamemptyvaluerequired?qsParamA=");
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamSpecificValueRequiredReverseFail() {

        // without the qsparam : should fail
        Map<String, Object> params = new HashMap<String, Object>();
        Router.reverse("myTestController.qsParamSpecificValueRequired", params);
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamSpecificValueRequiredReverseWrongValue() {

        // wrong qsparam's value : should fail
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "wrongValue");

        Router.reverse("myTestController.qsParamSpecificValueRequired", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamSpecificValueRequiredReverseEmptyValue() {
        // with empty : should fail
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "");

        Router.reverse("myTestController.qsParamSpecificValueRequired", params);
    }

    @Test
    public void qsParamSpecificValueRequiredReverse() {
        //  qsparam with the required value: should work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        ActionDefinition action = Router.reverse("myTestController.qsParamSpecificValueRequired", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparamspecificvaluerequired?qsParamA=abc");
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamNegateSpecificValueReverseFail() {

        // without the qsparam : should not work!
        Map<String, Object> params = new HashMap<String, Object>();
        Router.reverse("myTestController.qsParamNegateSpecificValue", params);
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamNegateSpecificValueReverseWrongValue() {

        // with the prohibited value : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        Router.reverse("myTestController.qsParamNegateSpecificValue", params);
    }

    @Test
    public void qsParamNegateSpecificValueReverse() {
        // with an empty value : should work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "");

        ActionDefinition action = Router.reverse("myTestController.qsParamNegateSpecificValue", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparamnegatespecificvalue?qsParamA=");

        // with a random value : should work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "def");

        action = Router.reverse("myTestController.qsParamNegateSpecificValue", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparamnegatespecificvalue?qsParamA=def");
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamTwoParamsRequiredReverseBothMissing() {

        // without any qsparam : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        Router.reverse("myTestController.qsParamTwoParamsRequired", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamTwoParamsRequiredReverseAnyMissing() {
        // with one required param only : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        Router.reverse("myTestController.qsParamTwoParamsRequired", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamTwoParamsRequiredReverseWrongValues() {

        // with the two required params but invalid vales : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "wrongValue");
        params.put("qsParamB", "abc");
        Router.reverse("myTestController.qsParamTwoParamsRequired", params);

    }

    @Test
    public void qsParamTwoParamsRequiredReverse() throws Exception {

        // with valid params : should work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        params.put("qsParamB", "anyValue");

        ActionDefinition action = Router.reverse("myTestController.qsParamTwoParamsRequired", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isNotNull().startsWith("/qsparamtwoparamsrequired?");

        List<NameValuePair> queryParams = URLEncodedUtils.parse(new URI(action.url), "utf-8");
        assertThat(queryParams).isNotNull();

        boolean qsParamAFound = false;
        boolean qsParamBFound = false;
        for (NameValuePair nameValuePair : queryParams) {
            if ("qsParamA".equals(nameValuePair.getName())) {
                qsParamAFound = true;
                assertThat(nameValuePair.getValue()).isEqualTo("abc");
            } else if ("qsParamB".equals(nameValuePair.getName())) {
                qsParamBFound = true;
                assertThat(nameValuePair.getValue()).isEqualTo("anyValue");
            }
        }
        assertThat(qsParamAFound).isTrue();
        assertThat(qsParamBFound).isTrue();
    }

    @Test
    public void qsParamEncodedValueAndRandomSpacesReverse() throws Exception {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", " ");
        params.put("qsParamB", "a b");
        params.put("qsParamC", "été");

        ActionDefinition action = Router.reverse("myTestController.qsParamEncodedValueAndRandomSpaces", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isNotNull().startsWith("/qsparamencodedvalueandrandomspaces?");

        List<NameValuePair> queryParams = URLEncodedUtils.parse(new URI(action.url), "utf-8");
        assertThat(queryParams).isNotNull();

        boolean qsParamAFound = false;
        boolean qsParamBFound = false;
        boolean qsParamCFound = false;
        for (NameValuePair nameValuePair : queryParams) {
            if ("qsParamA".equals(nameValuePair.getName())) {
                qsParamAFound = true;
                assertThat(nameValuePair.getValue()).isEqualTo(" ");
            } else if ("qsParamB".equals(nameValuePair.getName())) {
                qsParamBFound = true;
                assertThat(nameValuePair.getValue()).isEqualTo("a b");
            } else if ("qsParamC".equals(nameValuePair.getName())) {
                qsParamCFound = true;
                assertThat(nameValuePair.getValue()).isEqualTo("été");
            }
        }
        assertThat(qsParamAFound).isTrue();
        assertThat(qsParamBFound).isTrue();
        assertThat(qsParamCFound).isTrue();
    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamPlayNiceWithOtherRoutingFeaturesReversePathParamFail() {

        // with the path param only : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myName", "stromgol");
        Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamPlayNiceWithOtherRoutingFeaturesReverseStaticParamFail() {
        // with the staticArg param only : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myStaticArg", "someStaticVal");
        Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamPlayNiceWithOtherRoutingFeaturesReverseQsParamFail() {
        // with the querystring param only : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamPlayNiceWithOtherRoutingFeaturesMissingParam1Fail() {
        // with the staticArg param + path param only : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myName", "stromgol");
        params.put("myStaticArg", "someStaticVal");
        Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamPlayNiceWithOtherRoutingFeaturesMissingParam2Fail() {
        // with the querystring param  + query string param only : should not work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myName", "stromgol");
        params.put("qsParamA", "abc");
        Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);

    }

    @Test(expectedExceptions = NoHandlerFoundException.class)
    public void qsParamPlayNiceWithOtherRoutingFeaturesReverseMissingParamFail() {
        // with the staticArg param + querystring param only : should not work
        Map<String, Object> params = params = new HashMap<String, Object>();
        params.put("myStaticArg", "someStaticVal");
        params.put("qsParamA", "abc");
        Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);

    }

    @Test
    public void qsParamPlayNiceWithOtherRoutingFeaturesReverse() {
        // With all the required params : should work
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        params.put("myName", "stromgol");
        params.put("myStaticArg", "someStaticVal");
        ActionDefinition action = Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();

        assertThat(action.url).isEqualTo("/qsparamplaynicewithotherroutingfeatures/name/stromgol?qsParamA=abc");
    }

    @Test
    public void qsParamAndSameKeyStaticParamReverse() {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "someStaticVal");
        ActionDefinition action = Router.reverse("bindTestController.qsParamAndSameKeyStaticParam", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparamandsamekeystaticparam?qsParamA=abc");
    }

    @Test
    public void qsParamEmptyAndSameKeyStaticParamReverse() {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "someStaticVal");
        ActionDefinition action = Router.reverse("bindTestController.qsParamEmptyAndSameKeyStaticParam", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparamemptyandsamekeystaticparam?qsParamA=");
    }

    @Test
    public void qsParamNullAndSameKeyStaticParamReverse() {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "someStaticVal");
        ActionDefinition action = Router.reverse("bindTestController.qsParamNullAndSameKeyStaticParam", params);

        assertThat(action).isNotNull();
        assertThat(action.host).isNotNull();
        assertThat(action.url).isEqualTo("/qsparamnullandsamekeystaticparam?qsParamA=someStaticVal");
    }
}
