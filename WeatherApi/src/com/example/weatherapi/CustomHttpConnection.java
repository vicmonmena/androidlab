package com.example.weatherapi;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

/**
 * 
 * @author vicmonmena
 *
 */
public class CustomHttpConnection {

	/**
    * TAG for log messages.
    */
    private static final String TAG = CustomHttpConnection.class.getSimpleName();
   
    		
	/**
     * Download an image from URL.
     * @param url - contains an image.
     * @return image.
     */
    public static InputStream getImage(String url) {
            
            InputStream is = null;
            try {
                    HttpGet httpRequest = new HttpGet(url);
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = (HttpResponse) httpClient.execute(httpRequest);
                    HttpEntity entity = response.getEntity();
                    BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity); 
                    is = bufHttpEntity.getContent();
            } catch (Exception e) {
                    Log.e(TAG, "Error loading image from " + url);
            }
        return is;
    }
}
