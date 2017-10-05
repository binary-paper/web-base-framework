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
package net.binarypaper.webbaseframework.ejb.render;

import freemarker.core.InvalidReferenceException;
import freemarker.core.ParseException;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import net.binarypaper.webbaseframework.rest.BusinessLogicException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An EJB Stateless Session Bean to perform render documents using the
 * FreeMarker Template Engine
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
@Stateless
@LocalBean
public class FreeMarkerRenderBean {

    private static Configuration CONFIGURATION;

    public String render(ServletContext servletContext, String templateName, String contentType, String data) throws BusinessLogicException {
        if (CONFIGURATION == null) {
            Configuration configuration = new Configuration(new Version(2, 3, 23));
            // Set the preferred charset template files are stored in. UTF-8 is
            // a good choice in most applications.
            configuration.setDefaultEncoding("UTF-8");
            // Sets how errors will appear.
            // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            // Specify the source where the template files come from. Here I set a
            // folder path within the war file.
            configuration.setServletContextForTemplateLoading(servletContext, "/WEB-INF/email_templates");
            // Only assign the static CONFIGURATION if no IOException occurred
            CONFIGURATION = configuration;
        }
        try {
            Template template = CONFIGURATION.getTemplate(templateName);
            Map<String, Object> templateData = new HashMap<>();
            if (contentType.equalsIgnoreCase(MediaType.APPLICATION_XML)) {
                InputSource inputSource = new InputSource(new StringReader(data));
                templateData.put("data", NodeModel.parse(inputSource));
            } else {
                templateData.put("data", data);
            }
            StringWriter writer = new StringWriter();
            template.process(templateData, writer);
            return writer.toString();
        } catch (ParseException ex) {
            throw new BusinessLogicException("FMR2", Response.Status.BAD_REQUEST.getStatusCode());
        } catch (InvalidReferenceException ex) {
            throw new BusinessLogicException("FMR3", Response.Status.BAD_REQUEST.getStatusCode());
        } catch (IOException ex) {
            throw new BusinessLogicException("FMR4", Response.Status.NOT_FOUND.getStatusCode());
        } catch (TemplateException ex) {
            throw new BusinessLogicException("FMR5", ex.getMessage(), Response.Status.BAD_REQUEST.getStatusCode());
        } catch (SAXException ex) {
            throw new BusinessLogicException("FMR6", Response.Status.BAD_REQUEST.getStatusCode());
        } catch (ParserConfigurationException ex) {
            throw new BusinessLogicException("FMR7", Response.Status.BAD_REQUEST.getStatusCode());
        }
    }
}
