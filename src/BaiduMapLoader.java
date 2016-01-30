import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class BaiduMapLoader {

	protected String BAIDU_MAP_API_PREFIX = "http://api.map.baidu.com/place/v2/search";
	private String ak;
	
	private JSONObject bounds[];
	
	BaiduMapLoader(String ak)
	{
		this.ak = ak;
		bounds = null;
	}
	
	public void setBound(JSONObject leftBottomLocation, JSONObject rightTopLocation)
	{
		bounds = new JSONObject[2];
		bounds[0] = leftBottomLocation;
		bounds[1] = rightTopLocation;
	}
	
	public void removeBound()
	{
		bounds = null;
	}
	
	public JSONArray getPlaces(String query, String region, 
			int scope, String filter,
			String output) throws IOException, JSONException
	{
		JSONArray returnJSONArray = new JSONArray(); // Return
		
		// Chinese should be encoded as format of utf-8
		String encodedQuery = URLEncoder.encode(query,"utf-8");
		String encodedRegion = URLEncoder.encode(region,"utf-8");
		
		// Formalize URL Formation
		String formalizedURL = BAIDU_MAP_API_PREFIX + "?"
					+ "&query=" + encodedQuery;
		
		// Set Bounds
		if(bounds!=null)
			formalizedURL += ("&bounds="
						   + bounds[0].get("lat") + "," + bounds[0].get("lng")
					 + "," + bounds[1].get("lat") + "," + bounds[1].get("lng"));
		else
			formalizedURL += ("&region=" + encodedRegion);
		
		// Request Detail
		if(scope==2) 
			formalizedURL += ("&scope=2" + "&filter=" + filter);
		
		// Finish URL
		formalizedURL += "&output=" + output
				+ "&ak=" + ak;
		
		// Debug: Output the URL request
		//System.out.println("URL request: "+formalizedURL);
		
		// Get All Places of Pages
		int pageSize = 20;
		int pageNum = 0;
		int num = 0;
		while(true)
		{
			String finalURL = formalizedURL
							+ "&page_size=" + pageSize
							+ "&page_num=" + pageNum;
			
			URL urlRequest = new URL(finalURL);
			
			HttpURLConnection connection = (HttpURLConnection) urlRequest.openConnection(); 
			connection.connect();
			
			String JSONString = getJSONString(connection.getInputStream());
			if(JSONString.length()==0||!JSONString.startsWith("{"))
				break;
			
			// Debug: Output Responded JSON
			//System.out.println("JSONString:\n"+JSONString);
			
			// Convert Places' Information into JSON format
			JSONObject jsonObj = JSONObject.fromObject(JSONString);
			if((int)jsonObj.get("total")==0) break;
			
			JSONArray places = jsonObj.getJSONArray("results");
			if(places.size()==0)
				break;
			num+=places.size(); // Total Places
			
			for(int i=0;i<places.size();i++)
			{
				JSONObject place = (JSONObject) places.get(i);
				returnJSONArray.add(place);
			}
			
			connection.disconnect();
			pageNum++; // Read Next Page
		}
		
		// Debug: Output Number of Total places
		System.out.println("Places Number: "+(num-1));
		
		return returnJSONArray;
	}
	
	public JSONObject getLocation(String query, String region) throws IOException
	{
		// Chinese should be encoded as format of utf-8
		String encodedQuery = URLEncoder.encode(query,"utf-8");
		String encodedRegion = URLEncoder.encode(region,"utf-8");
		
		// Formalize URL Formation
		String formalizedURL = BAIDU_MAP_API_PREFIX + "?"
					+ "&query=" + encodedQuery 
					+ "&region=" + encodedRegion
					+ "&output=json"
					+ "&ak=" + ak;
		
		// Get Result
		URL urlRequest = new URL(formalizedURL);
		HttpURLConnection connection = (HttpURLConnection) urlRequest.openConnection(); 
		connection.connect();
		String JSONString = getJSONString(connection.getInputStream());
		JSONObject jsonObj = JSONObject.fromObject(JSONString);
		JSONObject place = (JSONObject) jsonObj.getJSONArray("results").get(0);
		
		// Debug: Output result
		//System.out.println("Location("+query+"): "+place.toString());
		
		return (JSONObject) place.get("location");
	}
	
	// Convert Input Stream into String
	private String getJSONString(InputStream inputStream) throws IOException
	{
		StringBuffer returnStr = new StringBuffer();
		
		DataInputStream reader = new DataInputStream(inputStream);
		byte[] buffer = new byte[4096];
		int count = 0;
		while ((count = reader.read(buffer)) > 0)
		{
			String result = new String(buffer, 0, count);
			returnStr.append(result);
		}
		reader.close();
		
		return returnStr.toString();
	}
}
