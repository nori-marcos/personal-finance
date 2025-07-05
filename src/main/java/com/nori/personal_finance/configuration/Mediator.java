package com.nori.personal_finance.configuration;

import static org.springframework.core.ResolvableType.forClass;

import jakarta.annotation.PreDestroy;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unchecked")
public class Mediator {
  private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  private static final Validator validator = factory.getValidator();
  private static final Map<String, Handler<?, ?>> handlers = new ConcurrentHashMap<>();

  @PreDestroy
  public void close() {
    factory.close();
  }

  public Mediator(final ApplicationContext context) {
    context
        .getBeansOfType(Handler.class)
        .values()
        .forEach(
            handler -> {
              final var type = ResolvableType.forClass(handler.getClass()).as(Handler.class);
              handlers.put(key(type.getGeneric(0), type.getGeneric(1)), handler);
            });
  }

  private static String key(final ResolvableType request, final ResolvableType response) {
    return request.getType() + "_" + response.getType();
  }

  public <Request, Response> Response handle(
      final Request request, final Class<Response> responseType) {
    return handle(request, forClass(responseType));
  }

  public <Request, Response> Response handle(
      final Request request, final ResolvableType responseType) {
    final var violations = validator.validate(request);
    if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
    return ((Handler<Request, Response>)
            handlers.get(key(forClass(request.getClass()), responseType)))
        .handle(request);
  }
}
