package hello;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class ScheduledTask {
    //private static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);

	private String lastId = null;
	
    @Scheduled(fixedRate = 600000)
    public void query() throws Exception {
    	RestTemplate restTemplate = new RestTemplate();
		String url = "https://api.stocktwits.com/api/2/streams/symbol/SPY.json";
		if (lastId != null) {
			url = url + "?since=" + lastId;
		}
		String tweets = restTemplate.getForObject(url, String.class);

		JSONObject json = new JSONObject(tweets);
		String messages = json.get("messages").toString();
		JSONArray msg = new JSONArray(messages);

		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		
		for (int i = 0 ; i < msg.length(); i++) {
			JSONObject latest = new JSONObject(msg.get(i).toString());
			
			sb.append(latest).append("\n");
			
			
			if (i==0) {
				lastId = latest.get("id").toString();
			}
			String sentimentTxt = ((JSONObject)latest.get("entities")).get("sentiment").toString();
			if (!("null").equals(sentimentTxt)) {
				JSONObject sentiment = (JSONObject) ((JSONObject)latest.get("entities")).get("sentiment");
				boolean isBullish = false;
				if ("Bullish".equals(sentiment.get("basic"))) {
					isBullish=true;
				} else {
					isBullish=false;
				}
				String body = latest.get("body").toString().replaceAll(",", "").replaceAll("\\$\\p{L}+", "").replaceAll("\n", "").replaceAll("\r\n", "").trim();
				if (body.length()>0) {
					if (isBullish) {
						sb2.append("1.0,").append(body).append("\n");
					} else {
						sb2.append("0.0,").append(body).append("\n");
					}
				}
			}
		}

		StorageUtil.write("tweets/SPY-" + lastId + "-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".json", sb.toString());
		StorageUtil.write("ml/data-" + lastId + "-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".txt", sb2.toString());
	};
}
