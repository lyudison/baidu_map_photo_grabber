import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class URLResourceLoader {
	
	URLResourceLoader()
	{
		
	}
	
	public void saveImageInPlaces(JSONArray places, String path) throws Exception
	{
		
		for(int i=0;i<places.size();i++)
		{
			// 1. Extracts Detail URL from Place
			JSONObject place = (JSONObject) places.get(i);
			String detailURL = getDetailURL(place);
			if(detailURL==null)
				continue;
			
			// Debug
			//System.out.println("Begin Extract Images..."+ "Detail URL: "+detailURL);
			
			// 2. Extracts All Related Images from Detail URL
			ArrayList<String> imgURLs = extractImgURLs(detailURL);
			
			// Debug
			//System.out.println("Image URLs:");
//			for(int j=0;j<imgURLs.size();j++)
//			{
//				System.out.println(imgURLs.get(j));
//			}
			
			// 3. Save Images Locally
			MySQLManager sqlManager = new MySQLManager();
			int id = sqlManager.getIdByPlaceName(place.getString("name"));
			String folder = path+"/"+id+"/";
			File file = new File(folder);
			if(!file.exists())
				file.mkdirs();
			for(int j=0;j<imgURLs.size();j++)
			{
				//String imgUrl = imgURLs.get(j).replace("\\", "");
				saveUrlFile(imgURLs.get(j),folder+j+".jpg");
			}
			
		}
		
	}
	
	private ArrayList<String> extractImgURLs(String detailURL) throws ParserException
	{
		ArrayList<String> rtnURLs = new ArrayList<String>();
		
		Parser parser = new Parser(detailURL);
		parser.setEncoding("utf-8");
		
		// Set Filter to get Images
		@SuppressWarnings("serial")
		NodeFilter imgFilter = new NodeFilter() {
			@Override
			public boolean accept(Node node) {
				
				// Debug: Node getText()
				//System.out.println("Node.getText(): "+node.getText());
				
				if (node.getText().contains("imgUrl")) 
					return true;
				
				return false;
			}
		};
		
		// Get All Tags with Filter
		NodeList list = parser.extractAllNodesThatMatch(imgFilter);
		
		// Debug: list size
		//System.out.println("Tag Size: "+list.size());
		
		for (int i = 0; i < list.size(); i++) {
			
			Node tag = list.elementAt(i);
			String innerHTML = tag.getText();
			
			while(true)
			{
				// Debug
				//System.out.println("Before Match: \n"+innerHTML);
				
				int start = innerHTML.indexOf("imgUrl")+9;
				if(start==-1+9)
					break;
				
				innerHTML = innerHTML.substring(start);
				int end = innerHTML.indexOf("\"");
				
				// Debug: Match String
				//System.out.println("After Match: \n"+"innerHTML: "+innerHTML+" start: "+start+" end:"+end);
				
				String imgURL = innerHTML.substring(0, end);
				rtnURLs.add(imgURL.replace("\\", ""));
				
				// Debug
				//System.out.println("Image URL: "+imgURL);
			}
		}
		
		return new ArrayList<String>(new LinkedHashSet<String>(rtnURLs));
		
	}
	
	private String getDetailURL(JSONObject place)
	{
		if(!place.has("detail_info"))
			return null;
		return ((JSONObject)place.get("detail_info")).get("detail_url").toString();
	}
	
	public boolean saveUrlFile(String fileUrl,String fileDes) throws Exception    
    {
        File toFile = new File(fileDes);
        if (toFile.exists())
        {
        	//System.out.println("File Existed: "+fileDes);
            return false;
        }
        
        byte[] data = getUrlFileData(fileUrl);
        if(data==null) return false;
        
        toFile.createNewFile();
        FileOutputStream outImgStream = new FileOutputStream(toFile);
        outImgStream.write(data);
        outImgStream.close();
        
        // Debug: Download succeed
        System.out.println("Download Succeed: " + fileDes);
        return true;
    }
	
	private byte[] getUrlFileData(String fileUrl) throws Exception
    {
        URL url = new URL(fileUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.connect();
        
        if(httpConn.getResponseCode()!=200)
        {
        	System.out.println("Download failed: "+fileUrl);
        	return null;
        }
        
        InputStream cin = httpConn.getInputStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = cin.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        cin.close();
        byte[] fileData = outStream.toByteArray();
        outStream.close();
        return fileData;
    }
}
