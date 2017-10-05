/*
 * Copyright 2016 <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.binarypaper.webbaseframework.rest;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Swagger annotations
@SwaggerDefinition(
  info = @Info(
    version = "1.0.0",
    title = "WebBaseFramework REST API",
    description = "REST API documentation",
    license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"),
    contact = @Contact(name = "Willy Gadney", email = "willy.gadney@binarypaper.net")
  ),
  tags = {
      @Tag(name = "Lookup Values", description = "A lookup value REST resource"),
      @Tag(name = "Render Notification", description = "A render notification REST resource")
  }
)
public class SwaggerBootstrap extends HttpServlet implements ReaderListener {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the Basic Authentication scheme
     */
    public static final String O_AUTH_2 = "OAuth2";

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setBasePath(servletConfig.getInitParameter("api.basepath"));
        beanConfig.setResourcePackage("net.binarypaper.webbaseframework.rest");
        beanConfig.setScan(true);
    }

    @Override
    public void beforeScan(Reader reader, Swagger swagger) {
    }

    @Override
    public void afterScan(Reader reader, Swagger swagger) {
        OAuth2Definition oAuth2Definition = new OAuth2Definition();
        oAuth2Definition.setFlow("implicit");
//        oAuth2Definition.setFlow("accessCode");
        oAuth2Definition.setAuthorizationUrl("http://localhost:8180/auth/realms/demo/protocol/openid-connect/auth");
//        oAuth2Definition.setTokenUrl("http://localhost:8180/auth/realms/demo/protocol/openid-connect/token");
//        Map<String, String> scopes = new HashMap<>();
//        scopes.put("READ_SCOPE", "Read scope");
//        oAuth2Definition.setScopes(scopes);
        swagger.addSecurityDefinition(O_AUTH_2, oAuth2Definition);
    }

}
