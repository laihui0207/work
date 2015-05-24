package com.tuyou.tsd.core.httpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonObject;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.videoMeta.PictureInf;

public class HttpServer extends NanoHTTPD {
	private static final String LOG_TAG = "HttpdService";
	private static final String ROOT_PATH = TSDConst.CAR_DVR_PATH+"/";
	
	public interface EventBroadcastor {
    	public void broadcast(Intent intent);
    }
	
    /**
     * Common mime type for dynamic content: binary
     */
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {{
        put("css", "text/css");
        put("htm", "text/html");
        put("html", "text/html");
        put("xml", "text/xml");
        put("java", "text/x-java-source, text/java");
        put("md", "text/plain");
        put("txt", "text/plain");
        put("asc", "text/plain");
        put("gif", "image/gif");
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("mp3", "audio/mpeg");
        put("m3u", "audio/mpeg-url");
        put("mp4", "video/mp4");
        put("ogv", "video/ogg");
        put("flv", "video/x-flv");
        put("mov", "video/quicktime");
        put("swf", "application/x-shockwave-flash");
        put("js", "application/javascript");
        put("json", "application/json");
        put("pdf", "application/pdf");
        put("doc", "application/msword");
        put("ogg", "application/x-ogg");
        put("zip", "application/octet-stream");
        put("exe", "application/octet-stream");
        put("class", "application/octet-stream");
    }};

    private final List<File> rootDirs;
    
    private Lock lock = new ReentrantLock();
    private Condition complete = lock.newCondition();    
    private Response response;
    
    private String localHost;
    private int localPort;
    
    private EventBroadcastor broadcastor;
    
    private void setResponse(Response response) {
    	lock.lock();
    	try {
    		this.response = response;
        	complete.signal();
    	}
    	finally {
    		lock.unlock();
    	}
    }
    
    private Response getResponse() {
    	lock.lock();
    	try {
    		this.response = null;
			complete.await(5, TimeUnit.SECONDS);
    		//complete.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			LogUtil.v(LOG_TAG, "interrupted the waitter: " + e.getMessage());
		}
    	finally {
    		lock.unlock();
    	}
    	
    	if (response == null) {
    		JSONObject status = new JSONObject();
    		JSONObject content = new JSONObject();
    		
			try {
				status.put("code", 500);
				status.put("message", "reponse time out");
				content.put("status", status);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
    		response = new Response(Response.Status.INTERNAL_ERROR, MIME_TYPES.get("json"), content.toString());
    	}
    	
    	return response;
    }

    public HttpServer(int port) {
    	super("0.0.0.0", port);
    	this.localPort = port;
    	this.rootDirs = new ArrayList<File>();
    	this.rootDirs.add(new File(ROOT_PATH));
    	
    	this.init();
    }
    
    public HttpServer(String host, int port, File wwwroot) {
        super(host, port);
        this.localPort = port;
        this.rootDirs = new ArrayList<File>();
        this.rootDirs.add(wwwroot);

        this.init();
    }

	/**
	 * Used to initialize and customize the server.
	 */
    public void init() {
    }

    private File getRootDir() {
        return rootDirs.get(0);
    }

    private List<File> getRootDirs() {
        return rootDirs;
    }

    private void addWwwRootDir(File wwwroot) {
        rootDirs.add(wwwroot);
    }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
     */
    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/"))
                newUri += "/";
            else if (tok.equals(" "))
                newUri += "%20";
            else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return newUri;
    }

    public Response serve(IHTTPSession session) {
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String uri = session.getUri();

        if (true) {
        	LogUtil.v(LOG_TAG, session.getMethod() + " '" + uri + "' ");

        	Iterator<String> e;
            //e = header.keySet().iterator();
            //while (e.hasNext()) {
            //    String value = e.next();
            //    LogUtil.d(LOG_TAG, "  HDR: '" + value + "' = '" + header.get(value) + "'");
            //}
            e = parms.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                LogUtil.v(LOG_TAG, "  PRM: '" + value + "' = '" + parms.get(value) + "'");
            }
        }

        if (uri.startsWith("/xbot/v1/cardvr")) {
            return  handleService(Collections.unmodifiableMap(header), session, uri);
        }
        else if (uri.startsWith("/videos/")) {
        	for (File homeDir : getRootDirs()) {
                // Make sure we won't die of an exception later
                if (!homeDir.isDirectory()) {
                    return getInternalErrorResponse("given path is not a directory (" + homeDir + ").");
                }
            }
            return handleFiles(Collections.unmodifiableMap(header), session, uri);
        }else if(uri.startsWith("/xbot/v1/mblogs")){
        	if(session.getMethod()==Method.DELETE){
        		Intent itClear = new Intent();
        		itClear.setAction(TSDEvent.CarDVR.CLEAR_PHOTO);
        		itClear.putExtra("id",session.getQueryParameterString());
        		broadcastor.broadcast(itClear);
        		return  createResponse(Response.Status.OK, MIME_PLAINTEXT,"{\"status\":{\"code\":0}}");
        	}else{
        		return  handleService(Collections.unmodifiableMap(header), session, uri);
        	}
        }else if(uri.startsWith("/pictures/")){
        	for (File homeDir : getRootDirs()) {
                if (!homeDir.isDirectory()) {
                    return getInternalErrorResponse("given path is not a directory (" + homeDir + ").");
                }
            }
            return handleFiles(Collections.unmodifiableMap(header), session, uri);
        }
        else {
        	return getNotFoundResponse();
        }
    }

    private Response handleService(Map<String, String> headers, IHTTPSession session, String uri) {
    	try {
    		localHost = session.getHeaders().get("local-addr");
    		List<String> pathSegments = session.getPathSegments();
    		Map<String, List<String>> queryParams = decodeParameters(session.getQueryParameterString());
    		if (pathSegments.get(3).equals("stats")) {
    			if(pathSegments.get(2).equals("mblogs")){
    				JSONObject msg = new JSONObject();
        			msg.put("module","mblogs");
        			msg.put("type","getPhotoStats");
        			postMessage(msg);
    			}else{
    				JSONObject msg = new JSONObject();
        			msg.put("module","cardvr");
        			msg.put("type","getVideoStats");
        			postMessage(msg);
    			}
    		}
    		else if (pathSegments.get(3).equals("sets")) {
    			JSONObject msg = new JSONObject();
    			msg.put("module", "cardvr");
    			msg.put("type","getVideoSets");

    			JSONObject content = new JSONObject();
    			content.put("type", pathSegments.get(5));
    			content.put("startTime", session.getParms().get("start"));
    			content.put("endTime", session.getParms().get("end"));

    			msg.put("content", content);
    			postMessage(msg);
    		}
    		else if (pathSegments.get(3).equals("videoes")) {
    			if (session.getMethod() == Method.GET) {
    				if (pathSegments.get(4).equals("thumbnails")) {
    					JSONObject msg = new JSONObject();
    					msg.put("module", "cardvr");
    					msg.put("type","getVideoThumbnail");

    					JSONObject content = new JSONObject();
    					content.put("needUpload", false);
    					List<String> names = queryParams.get("names");
    					JSONArray nameArray = new JSONArray();
    					for (int i = 0; i < names.size(); ++i) {
    						nameArray.put(names.get(i));
    					}
    					content.put("videoNames", nameArray);

    					msg.put("content", content);
    					postMessage(msg);
    				}
    				else {
    					if (pathSegments.size() == 6) {
    						JSONObject msg = new JSONObject();
    						msg.put("module", "cardvr");
    						msg.put("type","getVideoSetItems");

    						JSONObject content = new JSONObject();
    						content.put("startTime", session.getParms().get("start"));
    						content.put("endTime", session.getParms().get("end"));
    						content.put("type", session.getParms().get("type"));

    						msg.put("content", content);
    						postMessage(msg);
    					}
    					else if (pathSegments.size() == 5) { //get video
    						JSONObject msg = new JSONObject();
    						msg.put("module", "cardvr");
    						msg.put("type","getVideo");

    						JSONObject content = new JSONObject();
    						content.put("name", session.getParms().get("name"));
    						content.put("needUpload", false);
    						msg.put("content", content);
    						postMessage(msg);
    					}
    				}
    			}
    			else if (session.getMethod() == Method.PUT) {
    				JSONObject msg = new JSONObject();
    				msg.put("module", "cardvr");
    				msg.put("type","updateVideo");

    				Object o = new JSONTokener(session.getContent()).nextValue();
    				JSONArray content = o instanceof JSONArray ? (JSONArray)o : new JSONArray().put(o);
    				msg.put("content", content);
    				postMessage(msg);
    			}
    			else if (session.getMethod() == Method.DELETE) {
    				JSONObject msg = new JSONObject();
    				msg.put("module", "cardvr");
    				msg.put("type","deleteVideo");

    				List<String> names = queryParams.get("names");
    				JSONArray content = new JSONArray();
    				for (int i = 0; i < names.size(); ++i) {
    					content.put(names.get(i));
    				}
    				msg.put("content", content);
    				postMessage(msg);
    			}
    		}else if(uri.startsWith("/xbot/v1/mblogs")){
    			JSONObject msg = new JSONObject();
				msg.put("module", "mblogsDetailed");
				msg.put("type","getPhoto");

				JSONObject content = new JSONObject();
				content.put("value", session.getQueryParameterString());
				msg.put("content", content);
				postMessage(msg);
    		}

    		return getResponse();

    	} catch (JSONException e) {
    		LogUtil.e(LOG_TAG, "JSONException raised: " + e.getMessage());
    		return getNotFoundResponse();
    	}
    }

    private Response handleFiles(Map<String, String> headers, IHTTPSession session, String uri) {
        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        // Prohibit getting out of current directory
        if (uri.startsWith("src/main") || uri.endsWith("src/main") || uri.contains("../")) {
            return getForbiddenResponse("Won't serve ../ for security reasons.");
        }

        boolean canServeUri = false;
        File homeDir = null;
        List<File> roots = getRootDirs();
        for (int i = 0; !canServeUri && i < roots.size(); i++) {
            homeDir = roots.get(i);
            canServeUri = canServeUri(uri, homeDir);
        }
        if (!canServeUri) {
            return getNotFoundResponse();
        }

        // Browsers get confused without '/' after the directory, send a redirect.
        File f = new File(homeDir, uri);
        if (f.isDirectory() && !uri.endsWith("/")) {
            uri += "/";
            Response res = createResponse(Response.Status.REDIRECT, MIME_HTML, "<html><body>Redirected: <a href=\"" +
                uri + "\">" + uri + "</a></body></html>");
            res.addHeader("Location", uri);
            return res;
        }

        if (f.isDirectory()) {
        	return getForbiddenResponse("No directory listing.");
        }

        String mimeTypeForFile = getMimeTypeForFile(uri);
        Response response = null;

        response = serveFile(uri, headers, f, mimeTypeForFile);

        return response != null ? response : getNotFoundResponse();
    }
    
    private final void postMessage(JSONObject object) {
    	String strObj = object.toString();
    	Intent intent = new Intent(TSDEvent.Httpd.MESSAGE_ARRIVED);
		intent.putExtra("message", strObj);
		
		if (broadcastor != null) {
			broadcastor.broadcast(intent);
			LogUtil.v(LOG_TAG, "post message: " + strObj);
		}
    }

	/**
	 * 返回广播消息监听
	 */
	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {	
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.v(LOG_TAG, "received the broadcast: " + action);
			LogUtil.v(LOG_TAG, "message: " + intent.getExtras().getString("message"));

			// Video playing
			if (!action.equals(TSDEvent.Httpd.FEED_BACK)) {
				return;
			}
			
			try {
				JSONObject status = new JSONObject();
				status.put("code", 0);
				
				JSONObject ok200 = new JSONObject();
				ok200.put("status", status);
				
				JSONObject msg = new JSONObject(intent.getExtras().getString("message"));
				if (msg.get("type").equals("getVideoStats")) {
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("status", status);
					jsonRep.put("videoStats", msg.get("content"));
					
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}
				else if (msg.get("type").equals("getVideoSets")) {
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("status", status);
					jsonRep.put("videoSets", msg.get("content"));
					
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}
				else if (msg.get("type").equals("getVideoSetItems")) {
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("status", status);
					jsonRep.put("videoes", msg.get("content"));
					
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}
				else if (msg.get("type").equals("getVideo")) {
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("status", status);
					jsonRep.put("video", msg.get("content"));
					
					JSONObject video = (JSONObject)msg.get("content");
					String name = (String)video.get("name");
					String newUrl = "http://" + localHost + ":" + localPort + "/videos/" + name + ".mp4";
					video.put("url", newUrl);
					
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}
				else if (msg.get("type").equals("updateVideo")) {
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("status", status);
					
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}
				else if (msg.get("type").equals("deleteVideo")) {
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("status", status);
					
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}
				else if (msg.get("type").equals("getVideoThumbnail")) {
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("status", status);
					jsonRep.put("videoes", msg.get("content"));
					
					JSONArray videoes = (JSONArray)msg.get("content");
					for (int i = 0; videoes != null && i < videoes.length(); ++i) {
						JSONObject video = (JSONObject)videoes.get(i);
						String name = (String)video.get("name");
						String newUrl = "http://" + localHost + ":" + localPort + "/videos/" + name + ".jpg";
						video.put("thumbnail", newUrl);
					}
					
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}else if(msg.get("type").equals("getPhotoStats")){
					JSONObject status_photo = new JSONObject();
					status_photo.put("code", 0);
					JSONObject msg_photo = new JSONObject(intent.getExtras().getString("message"));
					JSONObject json = new JSONObject();
					json.put("total", msg_photo.get("content"));
					json.put("unread", 0);
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("stats", json);
					jsonRep.put("status", status_photo);
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}else if(msg.get("type").equals("getPhoto")){
					JSONObject status_photo = new JSONObject();
					status_photo.put("code", 0);
					JSONObject msg_obj = new JSONObject(intent.getExtras().getString("message"));
					JSONArray msg_photo = new JSONArray(msg_obj.getString("content"));
					for(int i=0;i<msg_photo.length();i++){
						JSONObject obj = msg_photo.getJSONObject(i).getJSONObject("url");
						String url = "http://" + localHost + ":" + localPort + obj.getString("url");
						obj.put("url", url);
					}
					JSONObject jsonRep = new JSONObject();
					jsonRep.put("status", status_photo);
					jsonRep.put("mblogs", msg_photo);
					Response rep = new Response(Response.Status.OK, MIME_TYPES.get("json"), jsonRep.toString());
					setResponse(rep);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				LogUtil.d(LOG_TAG, "parse the json failed " + e.getMessage());
				
				Response rep = new Response(Response.Status.INTERNAL_ERROR,
						MIME_TYPES.get("json"),
						"{\"status\":{\"code\":501}}");
				setResponse(rep);
			}
		}
	};
	
	public BroadcastReceiver getBroadcastReceiver() {
		return eventReceiver;
	}
	
	public void setEventBroadcastor(EventBroadcastor broadcastor) {
		this.broadcastor = broadcastor;
	}

    protected Response getNotFoundResponse() {
        return createResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
            "Error 404, file not found.");
    }
	
	

    protected Response getForbiddenResponse(String s) {
        return createResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: "
            + s);
    }

    protected Response getInternalErrorResponse(String s) {
        return createResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
            "INTERNAL ERRROR: " + s);
    }

    private boolean canServeUri(String uri, File homeDir) {
        boolean canServeUri;
        File f = new File(homeDir, uri);
        canServeUri = f.exists();
        if (!canServeUri) {
            String mimeTypeForFile = getMimeTypeForFile(uri);
        }
        return canServeUri;
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
     */
    Response serveFile(String uri, Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Change return code and add Content-Range header when skipping is requested
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);

                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }

        return res;
    }

    // Get MIME type from file name extension, if possible
    private String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? MIME_DEFAULT_BINARY : mime;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    protected String listDirectory(String uri, File f) {
        String heading = "Directory " + uri;
        StringBuilder msg = new StringBuilder("<html><head><title>" + heading + "</title><style><!--\n" +
            "span.dirname { font-weight: bold; }\n" +
            "span.filesize { font-size: 75%; }\n" +
            "// -->\n" +
            "</style>" +
            "</head><body><h1>" + heading + "</h1>");

        String up = null;
        if (uri.length() > 1) {
            String u = uri.substring(0, uri.length() - 1);
            int slash = u.lastIndexOf('/');
            if (slash >= 0 && slash < u.length()) {
                up = uri.substring(0, slash + 1);
            }
        }

        List<String> files = Arrays.asList(f.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        }));
        Collections.sort(files);
        List<String> directories = Arrays.asList(f.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        }));
        Collections.sort(directories);
        if (up != null || directories.size() + files.size() > 0) {
            msg.append("<ul>");
            if (up != null || directories.size() > 0) {
                msg.append("<section class=\"directories\">");
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"").append(up).append("\"><span class=\"dirname\">..</span></a></b></li>");
                }
                for (String directory : directories) {
                    String dir = directory + "/";
                    msg.append("<li><a rel=\"directory\" href=\"").append(encodeUri(uri + dir)).append("\"><span class=\"dirname\">").append(dir).append("</span></a></b></li>");
                }
                msg.append("</section>");
            }
            if (files.size() > 0) {
                msg.append("<section class=\"files\">");
                for (String file : files) {
                    msg.append("<li><a href=\"").append(encodeUri(uri + file)).append("\"><span class=\"filename\">").append(file).append("</span></a>");
                    File curFile = new File(f, file);
                    long len = curFile.length();
                    msg.append("&nbsp;<span class=\"filesize\">(");
                    if (len < 1024) {
                        msg.append(len).append(" bytes");
                    } else if (len < 1024 * 1024) {
                        msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB");
                    } else {
                        msg.append(len / (1024 * 1024)).append(".").append(len % (1024 * 1024) / 10 % 100).append(" MB");
                    }
                    msg.append(")</span></li>");
                }
                msg.append("</section>");
            }
            msg.append("</ul>");
        }
        msg.append("</body></html>");
        return msg.toString();
    }
}
