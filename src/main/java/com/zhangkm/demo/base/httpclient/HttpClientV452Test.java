package com.zhangkm.demo.base.httpclient;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientV452Test {

    /**
     * apache HttpClient 官方DEMO
     * 
     * @param args
     * @throws Exception
     */
    public final static void main(String[] args) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://httpbin.org/");

        System.out.println("Executing request " + httpget.getRequestLine());

        // Create a custom response handler
        ResponseHandler<String> responseHandler =
                new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(final HttpResponse response)
                            throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity)
                                    : null;
                        }
                        else {
                            throw new ClientProtocolException(
                                    "Unexpected response status: " + status);
                        }
                    }

                };
        String responseBody = "";
        try {
            responseBody = httpclient.execute(httpget, responseHandler);
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                httpclient.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("----------------------------------------");
        System.out.println(responseBody);
    }

}
