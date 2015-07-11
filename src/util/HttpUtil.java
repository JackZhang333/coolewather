package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpUtil {
	public static void sendHttpRequest(final String address,
			final HttpCallbackListener listener){
		new Thread(
				new Runnable(){

					@Override
					public void run() {
						HttpURLConnection connection = null;
						try{
							URL url =new URL(address);
							
							connection =(HttpURLConnection) url.openConnection();
							connection.setRequestMethod("GET");
							connection.setConnectTimeout(8*1000);
							connection.setReadTimeout(8*1000);
							
							InputStream is= connection.getInputStream();
							BufferedReader br =new BufferedReader(new InputStreamReader(is));
							StringBuilder response = new StringBuilder();
							String line ="";
							while((line = br.readLine())!=null){
								response.append(line);
							}
							Log.d("访问网络", response.toString());
							if(listener!=null){
								listener.onFinish(response.toString());
							}
						}catch(Exception e){
							if(listener!=null){
								//回调监听器的onError方法
								listener.onError(e);
							}
						}finally{
							if(connection!=null){
								connection.disconnect();
							}
						}
						
					}
					
				}
				).start();
	}

	
}
