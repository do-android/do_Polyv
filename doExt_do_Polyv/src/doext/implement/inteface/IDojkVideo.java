package doext.implement.inteface;

import org.json.JSONObject;

public interface IDojkVideo {

	JSONObject getState();

	void stop();

	int getCurrentTime();
}
