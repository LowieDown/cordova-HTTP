/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import java.net.UnknownHostException;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLHandshakeException;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
 
public class CordovaHttpPost extends CordovaHttp implements Runnable {
    public CordovaHttpPost(String urlString, Map<?, ?> params, Map<String, String> headers, CallbackContext callbackContext) {
        super(urlString, params, headers, callbackContext);
    }
    
    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.post(this.getUrlString());
            this.setupSecurity(request);
            request.acceptCharset(CHARSET);
            //request.followRedirects(false);
            request.headers(this.getHeaders());
            request.form(this.getParams());
            int code = request.code();
            String body = request.body(CHARSET);
            JSONObject response = new JSONObject();
            // response.put("cookie", request.header("Set-Cookie"));
            // response.put("cookies", request.headers("Set-Cookie"));
            StringBuilder sb = new StringBuilder();
            String[] cookies = request.headers("Set-Cookie");
            for (int i = 0; i < cookies.length; i++) {
                if (i > 0) {
                    sb.append(";");
                }
                String item = cookies[i];
                String[] parts = item.split("=", 2);
                String cookieName = parts[0];
                String cookieValue = parts[1];
                this.setCookie(cookieName, cookieValue);
                sb.append(item);
            }
            response.put("cookie", sb.toString());
            this.addResponseHeaders(request, response);
            response.put("status", code);
            if (code >= 200 && code < 300) {
                response.put("data", body);
                this.getCallbackContext().success(response);
            } else {
                response.put("error", body);
                this.getCallbackContext().error(response);
            }
        } catch (JSONException e) {
            this.respondWithError("There was an error generating the response");
        }  catch (HttpRequestException e) {
            if (e.getCause() instanceof UnknownHostException) {
                this.respondWithError(0, "The host could not be resolved");
            } else if (e.getCause() instanceof SSLHandshakeException) {
                this.respondWithError("SSL handshake failed");
            } else {
                this.respondWithError("There was an error with the request");
            }
        }
    }
}
