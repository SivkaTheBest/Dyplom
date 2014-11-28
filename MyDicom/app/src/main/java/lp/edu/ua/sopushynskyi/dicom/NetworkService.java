package lp.edu.ua.sopushynskyi.dicom;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class NetworkService {
    static String response = null;

    public NetworkService() {
    }

    public String makeServiceCall(String url) {
        return this.makeServiceCall(url, null);
    }

    public String makeServiceCall(String url, Map<String, String> params) {
        try {
            response = null;
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            if (params != null) {
                String paramString = formatParams(params);
                url += "?" + paramString;
            }
            HttpGet httpGet = new HttpGet(url);

            httpResponse = httpClient.execute(httpGet);
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private String formatParams(Map<String, String> params) {
        String result = "";
        for (String key : params.keySet()) {
            try {
                if (StringUtils.isEmpty(result))
                    result += "&";
                result += URLEncoder.encode(String.format("%s=%s", key, params.get(key)), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
