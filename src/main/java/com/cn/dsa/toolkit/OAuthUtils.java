package com.cn.dsa.toolkit;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

public class OAuthUtils {

    /**
     * Gets the auth String.
     * @return plain-text auth header
     */
    public static String getPlaintextOauthHeader(String key, String secret){
        System.out.println("getPlaintextOauthHeader()");
        long now = System.currentTimeMillis() / 1000;
        StringBuffer oauthHeader = new StringBuffer();

        oauthHeader.append("OAuth realm=\"\"");
        oauthHeader.append(",oauth_signature_method=\"PLAINTEXT\"");
        oauthHeader.append(",oauth_consumer_key=\"" + encodeKey(key) + "\"");
        oauthHeader.append(",oauth_version=\"1.0\"");
        oauthHeader.append(",oauth_timestamp=\"" + now + "\"");
        oauthHeader.append(",oauth_nonce=\"" + now + "\"");
        oauthHeader.append(",oauth_signature=\""+ encodeSecret(secret) + "\"");
        System.out.println("getPlaintextOauthHeader() [" + oauthHeader.toString() + "]");
        return oauthHeader.toString();
    }
    
    /**
     * OAuth key encoding for plaintext OAuth method.
     * OAuth URLEncodes the key and replaces certain characters.
     *
     * @param key
     * @return encoded key
     */
    public static String encodeKey(String key){
        String encodedKey = null;

        try {
            encodedKey = URLEncoder.encode(key, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
            return encodedKey;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Trouble encoding amgKey, returning unencoded key instead.  It will likely fail OAuth authentication.  encodeKey logic might need to be refactored:" + e);
            return key;
        }
    }

    /**
     * OAuth secret encoding for plaintext OAuth method.
     * OAuth does a double URLEncode, in addition to replacing certain characters.
     * After the first encode, for plaintext, OAuth adds an & character to the end
     * of the string then reencodes the whole string. The logic is:
     * 1. URLEncode secret
     * 2. Change + to %20
     * 3. Change * to %2A
     * 4. Change %7E to ~
     * 5. Add & to the end of the secret
     * 6. URLEncode secret again
     * 7. Change %7E to ~
     *
     * @param secret
     * @return encoded secret
     */
    public static String encodeSecret(String secret){
        String encodedSecret = null;

        try {
            encodedSecret = URLEncoder.encode(secret, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
            encodedSecret = URLEncoder.encode(encodedSecret + "&", "UTF-8").replace("%7E", "~");
            return encodedSecret;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Trouble encoding secret, returning unencoded secret instead.  It will likely fail OAuth authentication.  encodeSecret logic might need to be refactored: " + e);
            return secret;
        }
    }
    
    public static String createAuthorizationHeader(String key, String secret) {
        //set headers, including OAuth

        int millis = (int) System.currentTimeMillis() * -1;
        int timestamp = (int) millis / 1000;
 
        StringBuilder header = new StringBuilder();
        header.append("OAuth oauth_consumer_key=\""+key+"\", ");
        header.append("oauth_signature_method=\"HMAC-SHA1\", ");
        header.append("oauth_timestamp=\""+timestamp+"\", ");
        header.append("oauth_nonce=\""+millis+"\", ");
        header.append("oauth_version=\"1.0\", ");
        header.append("oauth_signature=\""+ encodeSecret(secret) + "\", ");
        header.append("OAuth oauth_consumer_key=\""+secret+"\"");
        
        System.out.println("Authorization: "+header.toString());
        return header.toString();
        
    }
    
    
}




