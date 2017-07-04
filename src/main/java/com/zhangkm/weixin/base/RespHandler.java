package com.zhangkm.weixin.base;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public class RespHandler implements ResponseHandler<String>{

	@Override
    public String handleResponse(
            final HttpResponse response) throws ClientProtocolException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity,"utf-8") : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }

}
