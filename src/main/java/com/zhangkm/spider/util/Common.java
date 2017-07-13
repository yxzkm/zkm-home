package com.zhangkm.spider.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Common {

	public static UrlResponse getUrlResponseTimes(String url,HttpClient httpClient){
		//如果遇到主机连接失败，则尝试三次，三次仍然失败，则放弃。
		//int connTimes = 3;
		int connTimes = 1;

		//如果遇到获取response失败，则尝试三次，三次仍然失败，则放弃。
		//int socketTimes = 3;
		int socketTimes = 1;

		while(true){
        	try {
        		return Common.getUrlResponse(url,httpClient); 
    		} catch (ConnectionPoolTimeoutException e) {//如果是连接池超时，则永远等待
    			Common.sleep(1000);
    		} catch (ConnectTimeoutException e) {//如果是连接url主机超时，则重试三次
    			if(connTimes>0){
    				connTimes=connTimes-1;
        			Common.sleep(200);
    			}else{
    				//logger.error("ListCatcher ERROR: 连接主机超时： "+entry.getEntryGroup()+"-"+entry.getEntryWebsite()+"-"+entry.getEntryChannel());
    				return null;
    			}
			} catch (SocketTimeoutException e) {//如果是获得response超时，则重试三次
    			if(socketTimes>0){
    				socketTimes=socketTimes-1;
        			Common.sleep(200);
    			}else{
    				//logger.error("ListCatcher ERROR: 获取应答超时： "+entry.getEntryGroup()+"-"+entry.getEntryWebsite()+"-"+entry.getEntryChannel());
    				return null;
    			}
			} catch (Exception e) {//未知异常，直接退出
				//logger.error("ListCatcher ERROR: 未知异常： " + entry.getEntryGroup()+"-"+entry.getEntryWebsite()+"-"+entry.getEntryChannel()+ "   " + e.getMessage());
				return null;
			}
    	}

	}
	public static UrlResponse getUrlResponse(String url,HttpClient httpClient) throws Exception {
		HttpGet httpGet = new HttpGet(url);
		
		httpGet.addHeader("Accept-Charset","utf-8");
		httpGet.addHeader("Accept","image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		httpGet.addHeader("Accept-Language","en-US,en;q=0.8,zh-Hans-CN;q=0.5,zh-Hans;q=0.3");
		httpGet.addHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/6.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; Tablet PC 2.0; InfoPath.3)");
		//httpGet.addHeader("User-Agent","YoudaoBot");
		//httpGet.addHeader("Accept-Encoding","gzip, deflate");
		//httpGet.addHeader("Host","baike.baidu.com");
		httpGet.addHeader("DNT","1");
		httpGet.addHeader("Connection","Keep-Alive");
		//httpGet.addHeader("Cookie","Hm_lpvt_55b574651fcae74b0a9f1cf9c8d7c93a=1388460323; Hm_lvt_55b574651fcae74b0a9f1cf9c8d7c93a=1388383076,1388383719,1388387207,1388450800; bdshare_firstime=1385718834663; edit-intro-cookie=1; BAIDUID=3C64176D62B478ACC430319B8E0AC53A:FG=1; locale=zh; BDUSS=zhtdzlDZkdVUTVNTy1yWmx-bFJNaXJsTHRCeFB0VEYwbUlJTE1qSHNpVHA2LUJTQVFBQUFBJCQAAAAAAAAAAAEAAAA4OSAwc3l4emttAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAOleuVLpXrlSV; H_PS_PSSID=4788_1433_4668_4261_4760_4678; BDRCVFR[e7VUaW6Ywr3]=mk3SLVN4HKm; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0");

		HttpContext context = new BasicHttpContext();
		HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet,context);
            
            if(response==null){
                //System.out.println("error: response is null");
        		throw new Exception();	
            } 
            
            /*
            Header[] headers = response.getAllHeaders();
            HttpEntity entity = response.getEntity();
            entity.getContentEncoding();
            for(Header header:headers){
               System.out.println(header.getName()+":"+header.getValue());
            }
            */
            
            StatusLine statusLine = response.getStatusLine();
            if(statusLine==null){
                //System.out.println("error: statusLine is null");
        		throw new Exception();	
            } 
            
            int responseCode = statusLine.getStatusCode();
            if(responseCode==404){
                //System.out.println("error: responseCode is 404");
        		throw new Exception();	
            } 
            
            /*
            Header header = response.getFirstHeader("content-type");
            if(header==null||header.getValue()==null){
                //System.out.println("error: header is null");
        		throw new Exception();	
        	}
            String contentType = header.getValue();
            if(!contentType.startsWith("text/html")) {
                //System.out.println("error: contentType is not text/html: "+contentType);
            	throw new Exception();	
            }
            */
            
		} catch (ConnectionPoolTimeoutException ex) {
			response=null;	
			try {httpGet.abort();} catch (Exception ignore) {}
			throw ex;
		} catch (ConnectTimeoutException ex) {
			response=null;			
			try {httpGet.abort();} catch (Exception ignore) {}
			throw ex;
		} catch (SocketTimeoutException ex) {
			response=null;			
			try {httpGet.abort();} catch (Exception ignore) {}
			throw ex;
        } catch (Exception ex) {
			//在这里捕获到其他类型的异常
			response=null;			
			try {httpGet.abort();} catch (Exception ignore) {}
			throw ex;
		}
        
		// 获得真实的url,用来判断是否被重定向
		HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		HttpUriRequest actualRequest = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);

		// 初始化UrlResponse
		UrlResponse urlResponse = new UrlResponse();
		urlResponse.setRequestUrlAbs(url);
		urlResponse.setRealHostName(targetHost.toURI());
		urlResponse.setRealUrl(actualRequest.getURI().getPath());

		// 查看页面是否被重定向
		if (!url.equals(targetHost.toURI() + actualRequest.getURI())) urlResponse.setRedirect(true);

		HttpEntity entity = response.getEntity();
		if (entity != null) {

			try {
/*	            System.out.println("----------------------------------------"+entity.isChunked());
	            System.out.println(response.getStatusLine());
                System.out.println("Response content length: " + entity.getContentLength());
	            //entity = new BufferedHttpEntity(entity);
*/
				InputStream is = entity.getContent();
				ByteArrayOutputStream bytestream = new ByteArrayOutputStream();  
				int ch;  
				while ((ch = is.read()) != -1) {  
					bytestream.write(ch);  
				}  
				byte[] responseBody = bytestream.toByteArray();  
				bytestream.close();  
				
	            
				String charset = EntityUtils.getContentCharSet(entity);
				if (charset == null || charset.equals("")){
					org.jsoup.nodes.Document doc = null;
					try {
						doc = Jsoup.parse(new String(responseBody));
						charset = "utf-8";
				    	Elements eles = new Elements();
						eles = doc.select("head");
						String headHtml = eles.html();
						if(headHtml.toLowerCase().indexOf("charset=gb2312")!=-1){
							charset = "gb2312";
						}else if(headHtml.toLowerCase().indexOf("charset=gbk")!=-1){
							charset = "gbk";
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				urlResponse.setResponseEncoding(charset);
				urlResponse.setResponseBody(new String(responseBody,charset));

				entity.consumeContent();
				return urlResponse;
			} catch (SocketTimeoutException ex) {
				throw ex;
			} catch (Exception ex) {
				throw ex;
			} finally {
				// Closing the input stream will trigger connection release
				try {
					entity.getContent().close();
					httpGet.abort();
				} catch (Exception ignore) {}
			}
		}
		return null;
	}

	public static void sleep(long millis){
		if(millis<=0) return;
		try {
			//System.out.println("sleep a while for "+millis/1000+" seconds...");
			Thread.sleep(millis);
		} catch (InterruptedException ignore) {}
	}

	public static int parseInt(String s){
		if(s==null||s.trim().equals("")) return 0;
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(s.trim());
		if(matcher.find()){
			String ret = matcher.group();
			if(ret==null||ret.trim().equals("")) return 0;
			try{
				return Integer.parseInt(ret.trim());	
			}catch(Exception e){
				return 0;
			}
		}else{
			return 0;
		}
	}

	public static byte[] getImage(String url,HttpClient httpClient) throws Exception {
		HttpGet httpGet = new HttpGet(url);
		
		HttpContext context = new BasicHttpContext();
		HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet,context);
            
            if(response==null){
                //System.out.println("error: response is null");
        		throw new Exception();	
            } 
            
            StatusLine statusLine = response.getStatusLine();
            if(statusLine==null){
                //System.out.println("error: statusLine is null");
        		throw new Exception();	
            } 
            
            int responseCode = statusLine.getStatusCode();
            if(responseCode==404){
                //System.out.println("error: responseCode is 404");
        		throw new Exception();	
            } 
            
		} catch (ConnectionPoolTimeoutException ex) {
			response=null;	
			try {httpGet.abort();} catch (Exception ignore) {}
			throw ex;
		} catch (ConnectTimeoutException ex) {
			response=null;			
			try {httpGet.abort();} catch (Exception ignore) {}
			throw ex;
		} catch (SocketTimeoutException ex) {
			response=null;			
			try {httpGet.abort();} catch (Exception ignore) {}
			throw ex;
        } catch (Exception ex) {
			//在这里捕获到其他类型的异常
			response=null;			
			try {httpGet.abort();} catch (Exception ignore) {}
			throw ex;
		}
        
		HttpEntity entity = response.getEntity();
		if (entity != null) {

			try {
				InputStream is = entity.getContent();
				ByteArrayOutputStream bytestream = new ByteArrayOutputStream();  
				int ch;  
				while ((ch = is.read()) != -1) {  
					bytestream.write(ch);  
				}  
				byte[] responseBody = bytestream.toByteArray();  
				bytestream.close();  

				/*
				FileOutputStream fos = new FileOutputStream("d:/aaa.jpg");  
				fos.write(responseBody);  
				fos.close();  
				*/
				
				entity.consumeContent();
				return responseBody;
			} catch (SocketTimeoutException ex) {
				throw ex;
			} catch (Exception ex) {
				throw ex;
			} finally {
				// Closing the input stream will trigger connection release
				try {
					entity.getContent().close();
					httpGet.abort();
				} catch (Exception ignore) {}
			}
		}
		return null;
	}

	
	public static void main(String[] args){
		System.out.println(getFullDateTimeFromString("2011-09-28 01:27:25"));
	}

	public static long getFullDateTimeFromString(String str){
		if(str==null||str.trim().equals("")) return System.currentTimeMillis();
		str=str.trim();

		if(str.indexOf("刚刚")!=-1){
    		return System.currentTimeMillis();
    	}else if(str.indexOf("分钟前")!=-1){
    		return System.currentTimeMillis();
    	}else if(str.indexOf("今天")!=-1){
    		return System.currentTimeMillis();
    	}else if(str.indexOf("昨天")!=-1){
    		return System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
		}

		try {
			Pattern datePattern = Pattern.compile("(((\\d{2})|(\\d{4}))[\\\\|\\-|/|年]){0,1}\\d{1,2}[\\\\|\\-|/|月]\\d{1,2}日{0,1}");
			Matcher dateMatcher = datePattern.matcher(str);
			String dateString = "";
			if(dateMatcher.find()) { //判断是否有匹配的字符串
				dateString = formatDateString(dateMatcher.group());//获取被匹配的Date部分,并进一步格式化
				if(dateString.equals("")) return System.currentTimeMillis(); //如果日期没有取到，则取时间没有意义，因此直接返回null
		    }else{
		    	return System.currentTimeMillis(); 
		    }
			Pattern timePattern = Pattern.compile("\\d{1,2}:\\d{1,2}(:\\d{1,2}){0,1}");
			Matcher timeMatcher = timePattern.matcher(str);
			String timeString = "";
			if(timeMatcher.find()) {
				timeString = formatTimeString(timeMatcher.group());//获取被匹配的Time部分,并进一步格式化
		    }else{
				timeString = "00:00:00";
		    }
		    return Timestamp.valueOf(dateString + " " + timeString).getTime();
		} catch (Exception e) {
    		return System.currentTimeMillis();
		}
	}

	/*
	 * 将任意一个包含日期时间信息的字符串，转换成为 "yyyy-MM-dd HH:mm:ss" 格式的日期时间字符串
	 */
	public static String getFullDT(String str){
		try{
			Timestamp ts = new Timestamp(getFullDateTimeFromString(str));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.format(ts);	
		}catch(Exception ignore){
			return null;
		}
	}
	private static String formatDateString(String str){
		str=str.trim();
		str=str.replace("/", "-");
		str=str.replace("年", "-");
		str=str.replace("月", "-");
		str=str.replace("\\", "-");
		str=str.replace("日", "");

		String dateFormatString = "";
		if(Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}").matcher(str).find()) {
			dateFormatString="yyyy-MM-dd";
		}else if(Pattern.compile("\\d{2}-\\d{1,2}-\\d{1,2}").matcher(str).find()){
			dateFormatString="yy-MM-dd";
		}else if(Pattern.compile("\\d{1,2}-\\d{1,2}").matcher(str).find()) {
			dateFormatString="yyyy-MM-dd";
			str=new SimpleDateFormat("yyyy").format(new Date())+"-"+str;	
		}else{
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
		try {
			Date date = sdf.parse(str);
			return new SimpleDateFormat("yyyy-MM-dd").format(date);
		} catch (ParseException ignore) {}
		
		return "";
	}	

	private static String formatTimeString(String str){
		str=str.trim();

		String dateFormatString = "";
		if(Pattern.compile("\\d{1,2}:\\d{1,2}").matcher(str).find()) dateFormatString="HH:mm";
		if(Pattern.compile("\\d{1,2}:\\d{1,2}:\\d{1,2}").matcher(str).find()) dateFormatString="HH:mm:ss";
		if(dateFormatString.equals(""))return "";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
		try {
			Date time = sdf.parse(str);
			return new SimpleDateFormat("HH:mm:ss").format(time);
		} catch (ParseException ignore) {}
		
		return "00:00:00";
	}	

	public static Document produceDocument(String xmlFileName){
        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null; 
        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            if(xmlFileName==null || xmlFileName.equals("")) return db.newDocument();
            return db.parse(xmlFileName);
        } catch (java.io.IOException ie){
            System.out.println("Could not read file: " + xmlFileName);
        } catch (SAXException e) {
        	System.out.println("Could not create Document: ");
        	System.out.println(e.getMessage());
        } catch (Exception ee){
        	System.out.println("Catch EEEEE");
            ee.printStackTrace();
        }
        return null;
    }

    private static String fmtDbl(double d,String fmtStr){
        return new DecimalFormat(fmtStr).format(d);
    }

    public static String fmtDbl2(String s){
        try{
            return fmtDbl(Double.valueOf(s),"#,###,###,###.00");
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    public static String fmtDbl2(double d){
        return fmtDbl(d,"#,###,###,##0.00");
    }
    public static String fmtDbl5(double d){
        return fmtDbl(d,"#0.#####");
    }

    public static void p_del(Object s){
    	SimpleDateFormat smf = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        System.out.println(smf.format(new Date())+s);
        write("g:/jcrb.log",smf.format(new Date())+(String)s);
    }
    
    public static void trace(Object s){
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");
        write("/trace/trace_" + df.format(new Date()) + ".txt",(String)s);
    }
    
    public static String intVal(String s){
        if(s==null || s.trim().equals("")) return "";
        try{
            return "" + Double.valueOf(s).intValue();
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    /**    
     * @param date1    
     * @param date2    
     * @return    
     */     
    public static int getMonths(Date date1, Date date2){      
        int iMonth = 0;      
        int flag = 0;      
        try{      
            Calendar objCalendarDate1 = Calendar.getInstance();      
            objCalendarDate1.setTime(date1);      
      
            Calendar objCalendarDate2 = Calendar.getInstance();      
            objCalendarDate2.setTime(date2);      
      
            if (objCalendarDate2.equals(objCalendarDate1))      
                return 0;      
            if (objCalendarDate1.after(objCalendarDate2)){      
                Calendar temp = objCalendarDate1;      
                objCalendarDate1 = objCalendarDate2;      
                objCalendarDate2 = temp;      
            }      
            if (objCalendarDate2.get(Calendar.DAY_OF_MONTH) < objCalendarDate1.get(Calendar.DAY_OF_MONTH))      
                flag = 1;      
      
            if (objCalendarDate2.get(Calendar.YEAR) > objCalendarDate1.get(Calendar.YEAR))      
                iMonth = ((objCalendarDate2.get(Calendar.YEAR) - objCalendarDate1.get(Calendar.YEAR))      
                        * 12 + objCalendarDate2.get(Calendar.MONTH) - flag)      
                        - objCalendarDate1.get(Calendar.MONTH);      
            else     
                iMonth = objCalendarDate2.get(Calendar.MONTH)      
                        - objCalendarDate1.get(Calendar.MONTH) - flag;      
      
        } catch (Exception e){      
         e.printStackTrace();      
        }      
        return iMonth;      
    }    


    public static int getDaysBetween(String fromDate, String toDate){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try{
            Date d1 = df.parse(fromDate);
            Date d2 = df.parse(toDate);
            long diff = d2.getTime() - d1.getTime();
            long days = diff / (1000 * 60 * 60 * 24) + 1;
            return Long.valueOf(days).intValue(); 
        }catch (Exception e){
            return 0;
        }
    }
    
    public static void write(String file, String conent) {   
        BufferedWriter out = null;   
        try {   
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));   
            out.write(conent+"\n");   
        } catch (Exception e) {   
            e.printStackTrace();   
        } finally {   
            try {   
                out.close();   
            } catch (IOException e) {   
                e.printStackTrace();   
            }   
        }   
    }   
    
    public static Properties getProperties(String path) {
        Properties prop = null;
        try {
            BufferedInputStream inBuff = new BufferedInputStream(
                    new FileInputStream(path));
            prop = new Properties();
            prop.load(inBuff);
            inBuff.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }
    
    public static int getLines(String s){
        if(s==null || s.length()==0 || s.equals("")) return 1;
        int lines = 1;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='\n') lines+=1;
        }
        return lines;
    } 

    public static String getSubStringByLines(String s,int lineStart,int lineEnd){
        if(s==null || s.length()==0 || s.equals("")) return "";
        if(lineStart<1) return s;
        if(lineEnd<1) return s;
        if(lineStart>lineEnd) return s;

        String[] arr = s.split("\n");
        if(arr==null || arr.length==0) return s;
        if(arr.length<lineEnd) lineEnd=arr.length;
        String temp = "";
        for(int i=lineStart;i<=lineEnd;i++){
            temp = temp + arr[i-1] + "\n";
        }
        return temp;
    } 
}
