package kr.jhha.engquiz.net.protocols;

import kr.jhha.engquiz.net.EProtocol;
import kr.jhha.engquiz.net.Protocol;
import kr.jhha.engquiz.net.Response;

public class SignInProtocol extends Protocol
{
   public final static Integer PID = 1002;

   public SignInProtocol( String nickname, String macID )
   {
       super( PID );
       makeRequest( nickname, macID );
   }

   private void makeRequest( String nickname, String macID )
   {
       request.set( EProtocol.UserNickName, nickname );
       request.set( EProtocol.MacID, macID );
   }

   @Override
   protected Response parseResponseMore()
   {
       return response;
   }
}
