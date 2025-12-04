package com.patrick;

import com.patrick.infra.config.Env;

public class Main {
    public static void main(String[] args) throws Exception {
      String apikey = Env.get("GOOGLE_API_KEY_GEMINI", true);
    }
}