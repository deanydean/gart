package bot.services

import javax.jws.*
import javax.jws.soap.*

@WebService
@SOAPBinding(parameterStyle=SOAPBinding.ParameterStyle.BARE)
public class DatastoreService {

    @WebMethod
    public void set(@WebParam(name="key") String key, 
            @WebParam(name="value") Object value2) {
        println "Got ${key} -> ${value}" 
    }

    @WebMethod
    public Object get(@WebParam(name="key") String key){
        return "test"
    }
}
