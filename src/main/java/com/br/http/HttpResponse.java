package com.br.http;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpResponse {
    private boolean success;
    private String data;
}
