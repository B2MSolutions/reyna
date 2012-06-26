reyna : Icelandic for "try"
=====
An android store and forward library for http post requests. 
Reyna will keep trying to post your requests until there is a valid connection.

## Usage 


```java
	import com.b2msolutions.reyna.services.StoreService;
    
	// Add any headers if required
	Header[] headers = new Header[] {
		new Header("Content-Type", "application/json"),
		new Header("myheader", "header content")
	};
		
	// Create the message to send
	Message message = new Message(
		new URI("http://server.tosendmessageto.com"), 
		"body of post, probably JSON", 
		headers);		
    
	// Send the message to Reyna
	StoreService.start(context, message);
```
