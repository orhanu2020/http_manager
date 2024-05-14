package com.br.http;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class HttpResponse {
    private boolean success;
    private String data;
    private Map headers;
}
