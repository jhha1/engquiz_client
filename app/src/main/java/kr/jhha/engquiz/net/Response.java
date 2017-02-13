package kr.jhha.engquiz.net;

import java.util.HashMap;
import java.util.Map;

import kr.jhha.engquiz.backend_logic.Utils;

/**
 * Created by thyone on 2017-02-08.
 */

public class Response {

    private Map<EProtocol, Object> responseMap = new HashMap<EProtocol, Object>();
    private String responseString = null;

    public Response(){}

    public Object get( EProtocol key ) {
        if( responseMap.containsKey(key) ){
            return  responseMap.get(key);
        }
        return null;
    }

    public boolean set( EProtocol key, Object value ) {
        if( key == null || value == null ) {
            System.out.println("ERROR Respse.set() param is null or empty. key[" +key+"], value["+value+"]");
            return false;
        }
        responseMap.put(key, value);
        return true;
    }

    public String getResponseString() {
        return this.responseString;
    }

    public void setResponseString( String responseString ) {
        if( responseString == null || responseString.isEmpty() ) {
            System.out.println("ERROR responseString is null or empty[" +responseString+"]");
            return;
        }
        this.responseString = responseString;
    }

    public void unserialize() {
        // extracting pure json string
        // JSON={..}  '='를 기점으로 'JSON'과 {}를 분리.
        int jsonBodyStartIndex = responseString.indexOf( "=" ) + 1;
        int jsonHeadEndIndex = "JSON=".length();
        if( jsonBodyStartIndex != jsonHeadEndIndex ) {
            System.out.println("ERROR invalid responseString:" + responseString);
            return;
        }
        String responseJson = responseString.substring(jsonBodyStartIndex);

        // json to map
        Map<String, Object> map = Utils.json2map( responseJson );
        if( map == null ) {
            System.out.println("ERROR resmap is null. responseString:" + responseJson);
            return;
        }

        // change map keys to 'EProtocol format'
        this.responseMap = Utils.string2enumOfMapKeys( map );
        System.out.println("[RES MAP] " + responseMap.toString());
    }
}
