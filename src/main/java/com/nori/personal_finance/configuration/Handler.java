package com.nori.personal_finance.configuration;

public interface Handler<Request, Response> {
  Response execute(Request request);
}
