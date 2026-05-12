package com.trustbridge.Features.Notifications.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateEngineService {

    private final TemplateEngine thymeleafTemplateEngine;

    public String processTemplate(String templateName, Map<String, Object> variables) {

        Context context = new Context();
        context.setVariables(variables);

        return thymeleafTemplateEngine.process("emails/" + templateName, context);

    }
}
