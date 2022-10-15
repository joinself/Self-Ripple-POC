package im.ananse.gateway;

public class PushyPushRequest {
    public Object to;
    public Object data;

    public Object notification;

    // "to" is an array of device tokens or single string
    //          String[] to = deviceTokens.toArray(new String[deviceTokens.size()]);
    // "data" is a HashMap<>, any object, it will be serialzed to JSON
    //   Set payload (any object, it will be serialized to JSON)
    //      Map<String, String> payload = new HashMap<>();
    //      payload.put("message", "Hello World!");
    // "message" (also HashMap)_will contain the notification fields needed for Android or iOS
    //   these are optional - more info here https://pushy.me/docs/api/send-notifications
    public PushyPushRequest(Object data, Object to, Object notification) {
        this.to = to;
        this.data = data;
        this.notification = notification;
    }
}