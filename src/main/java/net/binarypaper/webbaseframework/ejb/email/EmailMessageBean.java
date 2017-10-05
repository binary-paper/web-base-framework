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
package net.binarypaper.webbaseframework.ejb.email;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.java.Log;

/**
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// EJB annotations
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/EmailQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
// Lombok annotations
@Log
public class EmailMessageBean implements MessageListener {

    @Resource
    private MessageDrivenContext mdc;

    @Resource(mappedName = "java:jboss/mail/Default")
    private Session mailSession;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage;
        try {
            if (message instanceof ObjectMessage) {
                objectMessage = (ObjectMessage) message;
                EmailMessage emailMessage = (EmailMessage) objectMessage.getObject();
                MimeMessage mailMessage = new MimeMessage(mailSession);
                mailMessage.setFrom(new InternetAddress("admin@test.com"));
                mailMessage.setRecipients(RecipientType.TO, InternetAddress.parse(emailMessage.getToAddress()));
                mailMessage.setSubject(emailMessage.getSubject());
                mailMessage.setText(emailMessage.getBody(), "utf-8", "html");
                Transport.send(mailMessage);
            }
        } catch (JMSException | MessagingException ex) {
            log.severe(ex.getMessage());
            mdc.setRollbackOnly();
        }
    }

}
