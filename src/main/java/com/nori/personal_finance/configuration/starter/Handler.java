package com.nori.personal_finance.configuration.starter;

public interface Handler<Request, Response> {
  Response handle(Request request);
}
