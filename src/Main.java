/*
 * Program: 
 * 		Grab all POIs specified from Baidu Map API
 * Author: 
 * 		Lyudison (Desheng Liu)
 * 		Software School, SYSU
 * 		lyudison@gmail.com
 * History:
 * 		2014.7.18 First Version
 * Presumption:
 * 		Configure MySQL locally, and set your name and password in 'MySQLManager'
 */

import net.sf.json.JSONArray;

public class Main {

	/*
	 * Setting Area
	 */
	static String QUERY = "洗车 汽车服务 海珠区";
	static String REGION = "广州";
	static String OUTPUT_FORMAT = "json";
	static String AK_KEY = "wZNqARgljDouaTQYxPFEEm7k"; // Baidu Map API access key, applied at Baidu Map
	static int SCOPE = 2; // 1: no filter, 2: with filter
	static String FILTER = "industry_type:life";
	// Rectangle Bound POI Search
	static boolean useBound = false;
	static String QUERY_BOUND_LEFT_BOTTOM = "春明汽车服务部 广州";
	static String QUERY_BOUND_RIGHT_TOP = "澎记汽车美容店 广州";
	static String IMAGE_PATH = "ShopPhotos";
	/*
	 * Setting Area End
	 */
	
	public static void main(String[] args) throws Exception {
		
		// 1. Set Search Parameters
		String query = QUERY;
		String region = REGION;
		String output = OUTPUT_FORMAT;
		String ak = AK_KEY;
		int scope = SCOPE;
		String filter = FILTER;
		
		// 2. Request Information from Baidu Map API
		BaiduMapLoader mapLoader = new BaiduMapLoader(ak);
		if(useBound)
			mapLoader.setBound(mapLoader.getLocation(QUERY_BOUND_LEFT_BOTTOM, region), mapLoader.getLocation(QUERY_BOUND_RIGHT_TOP, region));
		JSONArray places = mapLoader.getPlaces(query, region, scope, filter, output);
		//System.out.println(places);
		
		// 3. Store the Information into MySQL
		MySQLManager sqlManager = new MySQLManager();
		if(sqlManager.importPlaces(places))
			System.out.println("Import to MySQL succeeded.");
		
		// 4. Download Images from "detail_url" page
		URLResourceLoader urlLoader = new URLResourceLoader();
		urlLoader.saveImageInPlaces(places,IMAGE_PATH);
		
		// Debug
		System.out.println("Application Finished");
	}
}
